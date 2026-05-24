import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

interface Parser {
    void parseProgram();
}

final class Parsers {
    private Parsers() {
    }

    static Parser forTree(Lexer lexer) {
        return new RecursiveDescentParser(lexer, true, false, null);
    }

    static Parser forTac(Lexer lexer, CodeGenerator codeGen) {
        return new BisonTacParser(lexer, codeGen);
    }
}

final class RecursiveDescentParser implements Parser {
    private final Lexer lexer;
    private Token lookahead;
    private int indent = 0;

    private final boolean enableTreeOutput;
    private final boolean enableCodeGen;
    private final CodeGenerator codeGen;

    // ========== 错误恢复框架 ==========
    private int errorCount = 0;
    private final List<String> errorMessages = new ArrayList<>();
    private static final int MAX_ERRORS = 10;

    /** 同步集合：语句级同步点 */
    private static final Set<String> SYNC_STMT = new HashSet<>(Arrays.asList(
            "SEMI", "END", "ELSE"
    ));

    /** 同步集合：块级同步点（表达式等深层结构出错时使用） */
    private static final Set<String> SYNC_BLOCK = new HashSet<>(Arrays.asList(
            "SEMI", "END", "ELSE", "THEN", "DO", "BEGIN", "IF", "WHILE"
    ));
    // ==================================

    private static final class ConditionJump {
        final int falseGotoIndex;

        ConditionJump(int falseGotoIndex) {
            this.falseGotoIndex = falseGotoIndex;
        }
    }

    RecursiveDescentParser(Lexer lexer, boolean enableTreeOutput, boolean enableCodeGen, CodeGenerator codeGen) {
        this.lexer = lexer;
        this.enableTreeOutput = enableTreeOutput;
        this.enableCodeGen = enableCodeGen;
        this.codeGen = codeGen;
        this.lookahead = lexer.nextToken();
    }

    private void printTree(String text) {
        if (!enableTreeOutput) return;

        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(text);
    }

    private void printTree(String text, String production) {
        if (!enableTreeOutput) return;

        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(text + "    [" + production + "]");
    }

    private String tokenDisplay(Token token) {
        if (token == null) {
            return "EOF";
        }

        if (token.value == null || token.value.equals("-")) {
            return token.type;
        }

        return token.type + " : " + token.value;
    }

    /**
     * 记录语法错误，但不中断分析。
     * 超过 MAX_ERRORS 时才抛出异常停止。
     */
    private void error(String message) {
        errorCount++;
        String location;
        if (lookahead == null) {
            location = "文件末尾";
        } else {
            location = "行" + lookahead.line + "列" + lookahead.column;
        }
        String tokenInfo = lookahead == null
                ? "（已到达EOF）"
                : "（当前token: " + lookahead.type + " '" + lookahead.lexeme + "'）";
        String fullMsg = "语法错误 [" + location + "]: " + message + " " + tokenInfo;
        errorMessages.add(fullMsg);
        System.err.println(fullMsg);

        if (errorCount > MAX_ERRORS) {
            throw new RuntimeException("错误过多（>" + MAX_ERRORS + "），停止分析");
        }
    }

    /**
     * 不可恢复的错误，直接抛异常。
     */
    private void fatalError(String message) {
        String location = lookahead == null ? "文件末尾"
                : "行" + lookahead.line + "列" + lookahead.column;
        throw new RuntimeException("致命语法错误 [" + location + "]: " + message);
    }

    private boolean check(String type) {
        return lookahead != null && lookahead.type.equals(type);
    }

    /**
     * 匹配指定 token 类型。
     * 失败时尝试简单纠正（如隐式分号/括号/end），否则跳过错误 token 继续。
     */
    private void match(String type) {
        if (check(type)) {
            printTree(tokenDisplay(lookahead));
            lookahead = lexer.nextToken();
            return;
        }

        // ---- 失败：先报错 ----
        String current = lookahead == null ? "EOF" : lookahead.type;
        error("期望 " + type + "，但遇到 " + current);

        // ---- 尝试简单纠正 ----
        // 纠正1：缺少分号（期望 SEMI 时当前是后续合法结构）
        if (type.equals("SEMI") && (check("IDN") || check("IF") || check("WHILE")
                || check("BEGIN") || check("END") || check("ELSE") || lookahead == null)) {
            return;
        }

        // 纠正2：缺少右括号
        if (type.equals("SRP") && (check("SEMI") || check("END") || check("ELSE")
                || check("THEN") || check("DO") || lookahead == null)) {
            return;
        }

        // 纠正3：缺少 end
        if (type.equals("END") && (lookahead == null || check("SEMI") || check("ELSE"))) {
            return;
        }

        // 纠正4：缺少 then
        if (type.equals("THEN") && (check("IDN") || check("IF") || check("WHILE")
                || check("BEGIN") || check("END"))) {
            return;
        }

        // ---- 否则跳过当前错误 token 继续 ----
        if (lookahead != null) {
            lookahead = lexer.nextToken();
        }
    }

    /**
     * 恐慌模式恢复：跳过输入直到遇到同步 token。
     */
    private void skipToSync(String context) {
        Set<String> syncSet = context.equals("stmt") ? SYNC_STMT : SYNC_BLOCK;

        while (lookahead != null && !syncSet.contains(lookahead.type)) {
            lookahead = lexer.nextToken();
        }
        // 如果停在了分号上，消费掉它
        if (check("SEMI")) {
            lookahead = lexer.nextToken();
        }
    }

    /**
     * P -> L+
     */
    public void parseProgram() {
        printTree("P", "P -> L+");
        indent++;

        if (lookahead == null) {
            error("程序为空");
            indent--;
            return;
        }

        while (lookahead != null) {
            String nextLabel = null;

            if (enableCodeGen) {
                nextLabel = codeGen.newProgramNextLabel();
            }

            try {
                parseL(nextLabel);
            } catch (RuntimeException e) {
                // parseL 内部可能因 MAX_ERRORS 抛出异常
                throw e;
            }

            if (enableCodeGen && (lookahead != null || codeGen.isLabelReferenced(nextLabel))) {
                codeGen.emitLabel(nextLabel);
            }
        }

        indent--;

        // ====== 错误汇总 ======
        if (errorCount > 0) {
            System.err.println("\n========== 语法分析完成 ==========");
            System.err.println("共发现 " + errorCount + " 个错误：");
            for (int i = 0; i < errorMessages.size(); i++) {
                System.err.println("  [" + (i + 1) + "] " + errorMessages.get(i));
            }
            System.err.println("===================================");
        }

        if (enableTreeOutput && errorCount == 0) {
            System.out.println("语法分析成功！");
        } else if (enableTreeOutput) {
            System.out.println("语法分析完成，但有 " + errorCount + " 个错误。");
        }
    }

    /**
     * L -> S ;
     * 末尾分号可选（允许 end 前最后一条语句省略分号）。
     */
    private void parseL(String nextLabel) {
        printTree("L", "L -> S ;");
        indent++;

        parseS(nextLabel);
        if (lookahead != null && !check("EOF")) {
            match("SEMI");
        }

        indent--;
    }

    /**
     * S -> id = E
     * S -> if C then S S'
     * S -> while C do S
     * S -> begin L_list end
     */
    private void parseS(String nextLabel) {
        parseS(nextLabel, null);
    }

    private void parseS(String nextLabel, String currentLabel) {
        if (check("IDN")) {
            parseAssign();
        } else if (check("IF")) {
            parseIf(nextLabel);
        } else if (check("WHILE")) {
            parseWhile(nextLabel, currentLabel);
        } else if (check("BEGIN")) {
            parseCompound(nextLabel, currentLabel);
        } else {
            error("无法识别的语句开头");
            // 跳过当前 token，尝试继续
            if (lookahead != null) {
                lookahead = lexer.nextToken();
            }
        }
    }

    /**
     * S -> id = E
     */
    private void parseAssign() {
        printTree("S", "S -> id = E");
        indent++;

        String id = lookahead.value;

        match("IDN");
        match("EQ");

        ExprAttr expr = parseE();

        if (enableCodeGen && expr != null) {
            codeGen.emit(id + " = " + expr.place);
        }

        indent--;
    }

    /**
     * S -> if C then S S'
     *
     * S' -> else S
     * S' -> ε
     */
    private void parseIf(String nextLabel) {
        printTree("S", "S -> if C then S S'");
        indent++;

        match("IF");

        String trueLabel = enableCodeGen ? codeGen.newLabel() : null;

        ConditionJump condition = parseC(trueLabel, nextLabel);

        match("THEN");

        if (enableCodeGen) {
            codeGen.emitLabel(trueLabel);
        }

        parseS(nextLabel, trueLabel);

        if (check("ELSE")) {
            printTree("S'", "S' -> else S");
            indent++;

            if (enableCodeGen && condition != null) {
                String falseLabel = codeGen.newLabel();
                codeGen.patchGoto(condition.falseGotoIndex, falseLabel);
                codeGen.emitGoto(nextLabel);
                codeGen.emitLabel(falseLabel);
            }

            match("ELSE");
            parseS(nextLabel);

            indent--;
        } else {
            printTree("S'", "S' -> ε");
            indent++;
            printTree("ε");
            indent--;
        }

        indent--;
    }

    /**
     * S -> while C do S
     */
    private void parseWhile(String nextLabel, String currentLabel) {
        printTree("S", "S -> while C do S");
        indent++;

        String beginLabel = enableCodeGen ? currentLabel : null;
        String trueLabel = null;

        if (enableCodeGen) {
            if (beginLabel == null) {
                beginLabel = codeGen.newLabel();
                codeGen.emitLabel(beginLabel);
            }

            trueLabel = codeGen.newLabel();
        }

        match("WHILE");

        parseC(trueLabel, nextLabel);

        match("DO");

        if (enableCodeGen) {
            codeGen.emitLabel(trueLabel);
        }

        parseS(beginLabel);

        if (enableCodeGen) {
            codeGen.emitGoto(beginLabel);
        }

        indent--;
    }

    /**
     * S -> begin L_list end
     *
     * 复合语句。内部语句列表 L_list 使用独立的解析方法，
     * 与顶层 L 解耦，避免修改 parseL 和 parseProgram。
     *
     * 控制流标签 nextLabel / currentLabel 透传给内部每条语句。
     */
    private void parseCompound(String nextLabel, String currentLabel) {
        printTree("S", "S -> begin L_list end");
        indent++;
        match("BEGIN");
        // 解析内部的语句列表（至少一条 S ;）
        parseLList(nextLabel, currentLabel);
        match("END");
        indent--;
    }

    /**
     * L_list -> S ; L_list
     * L_list -> S ;
     *
     * 仅用于 begin...end 内部的语句序列。
     * 与顶层 parseL 分离，减少耦合。
     */
    private void parseLList(String nextLabel, String currentLabel) {
        printTree("L_list", "L_list -> S ;");
        indent++;

        parseS(nextLabel, currentLabel);
        match("SEMI");

        // 如果后面还有语句（且不是 end），继续解析
        if (lookahead != null && !check("END")) {
            indent--;
            parseLList(nextLabel, currentLabel);
            return;
        }

        indent--;
    }

    /**
     * C -> E relop E
     *
     * 实验三中生成：
     * if E1 relop E2 goto trueLabel
     * goto falseLabel
     */
    private ConditionJump parseC(String trueLabel, String falseLabel) {
        printTree("C", "C -> E relop E");
        indent++;

        ExprAttr left = parseE();

        String op = parseRelop();

        ExprAttr right = parseE();

        int falseGotoIndex = -1;

        if (enableCodeGen && left != null && right != null && op != null) {
            codeGen.markLabelReferenced(trueLabel);
            codeGen.emit("if " + left.place + " " + op + " " + right.place + " goto " + trueLabel);
            falseGotoIndex = codeGen.emitGoto(falseLabel);
        }

        indent--;
        return new ConditionJump(falseGotoIndex);
    }

    /**
     * relop -> > | < | = | >= | <= | <>
     */
    private String parseRelop() {
        if (check("GT")) {
            printTree("relop", "relop -> >");
            indent++;
            String op = lookahead.value;
            match("GT");
            indent--;
            return op;
        } else if (check("LT")) {
            printTree("relop", "relop -> <");
            indent++;
            String op = lookahead.value;
            match("LT");
            indent--;
            return op;
        } else if (check("EQ")) {
            printTree("relop", "relop -> =");
            indent++;
            String op = lookahead.value;
            match("EQ");
            indent--;
            return op;
        } else if (check("GE")) {
            printTree("relop", "relop -> >=");
            indent++;
            String op = lookahead.value;
            match("GE");
            indent--;
            return op;
        } else if (check("LE")) {
            printTree("relop", "relop -> <=");
            indent++;
            String op = lookahead.value;
            match("LE");
            indent--;
            return op;
        } else if (check("NEQ")) {
            printTree("relop", "relop -> <>");
            indent++;
            String op = lookahead.value;
            match("NEQ");
            indent--;
            return op;
        } else {
            error("条件表达式中缺少关系运算符");
            return null;
        }
    }

    /**
     * E -> T E'
     *
     * 用 while 实现 E'，同时生成加减法三地址代码。
     */
    private ExprAttr parseE() {
        printTree("E", "E -> T E'");
        indent++;

        ExprAttr left = parseT();
        if (left == null) {
            indent--;
            return null;
        }

        while (check("ADD") || check("SUB")) {
            String op = lookahead.value;

            if (check("ADD")) {
                printTree("E'", "E' -> + T E'");
                indent++;
                match("ADD");
            } else {
                printTree("E'", "E' -> - T E'");
                indent++;
                match("SUB");
            }

            ExprAttr right = parseT();

            if (enableCodeGen && right != null) {
                String temp = codeGen.newTemp();
                String assignOp = left.place.startsWith("t") ? " = " : " := ";
                codeGen.emit(temp + assignOp + left.place + " " + op + " " + right.place);
                left = new ExprAttr(temp);
            }

            indent--;
        }

        printTree("E'", "E' -> ε");
        indent++;
        printTree("ε");
        indent--;

        indent--;
        return left;
    }

    /**
     * T -> F T'
     *
     * 用 while 实现 T'，同时生成乘除法三地址代码。
     */
    private ExprAttr parseT() {
        printTree("T", "T -> F T'");
        indent++;

        ExprAttr left = parseF();
        if (left == null) {
            indent--;
            return null;
        }

        while (check("MUL") || check("DIV")) {
            String op = lookahead.value;

            if (check("MUL")) {
                printTree("T'", "T' -> * F T'");
                indent++;
                match("MUL");
            } else {
                printTree("T'", "T' -> / F T'");
                indent++;
                match("DIV");
            }

            ExprAttr right = parseF();

            if (enableCodeGen && right != null) {
                String temp = codeGen.newTemp();
                codeGen.emit(temp + " = " + left.place + " " + op + " " + right.place);
                left = new ExprAttr(temp);
            }

            indent--;
        }

        printTree("T'", "T' -> ε");
        indent++;
        printTree("ε");
        indent--;

        indent--;
        return left;
    }

    /**
     * F -> ( E )
     * F -> id
     * F -> int8
     * F -> int10
     * F -> int16
     *
     * 遇到非法 token 时记录错误并返回占位 ExprAttr，使后续分析可以继续。
     */
    private ExprAttr parseF() {
        if (check("SLP")) {
            printTree("F", "F -> ( E )");
            indent++;

            match("SLP");
            ExprAttr expr = parseE();
            match("SRP");

            indent--;
            return expr;

        } else if (check("IDN")) {
            printTree("F", "F -> id");
            indent++;

            String place = lookahead.value;
            match("IDN");

            indent--;
            return new ExprAttr(place);

        } else if (check("OCT")) {
            printTree("F", "F -> int8");
            indent++;

            String place = lookahead.value;
            match("OCT");

            indent--;
            return new ExprAttr(place);

        } else if (check("DEC")) {
            printTree("F", "F -> int10");
            indent++;

            String place = lookahead.value;
            match("DEC");

            indent--;
            return new ExprAttr(place);

        } else if (check("HEX")) {
            printTree("F", "F -> int16");
            indent++;

            String place = lookahead.value;
            match("HEX");

            indent--;
            return new ExprAttr(place);

        } else if (check("ILOCT")) {
            if (enableTreeOutput && !enableCodeGen) {
                throw new RuntimeException("语法错误：非法八进制整数，当前单词为：ILOCT，属性值：-");
            }
            error("非法八进制整数: " + lookahead.lexeme);
            lookahead = lexer.nextToken();
            return new ExprAttr("0");

        } else if (check("ILHEX")) {
            if (enableTreeOutput && !enableCodeGen) {
                throw new RuntimeException("语法错误：非法十六进制整数，当前单词为：ILHEX，属性值：-");
            }
            error("非法十六进制整数: " + lookahead.lexeme);
            lookahead = lexer.nextToken();
            return new ExprAttr("0");

        } else if (check("UNKNOWN")) {
            error("无法识别的字符: '" + lookahead.lexeme + "'");
            lookahead = lexer.nextToken();
            return new ExprAttr("0");

        } else {
            error("因子错误，期望 id、整数或括号表达式");
            // 不消耗当前 token：当前 token 可能是分号、end 等合法分隔符，
            // 应留给上层（parseAssign → match）处理。skipToSync 会误吞分号。
            return new ExprAttr("0");
        }
    }
}
