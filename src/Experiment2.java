import java.io.*;
import java.util.*;

public class Experiment2 {

    /**
     * 实验一：词法分析器
     */
    static class Lexer {
        private final String input;
        private int pos = 0;
        private final int length;

        private static final Set<String> keywords = new HashSet<>(Arrays.asList(
                "if", "then", "else", "while", "do", "begin", "end"
        ));

        Lexer(String input) {
            this.input = input;
            this.length = input.length();
        }

        private char peek() {
            if (pos >= length) return '\0';
            return input.charAt(pos);
        }

        private char next() {
            if (pos >= length) return '\0';
            return input.charAt(pos++);
        }

        private boolean isLetter(char c) {
            return Character.isLetter(c);
        }

        private boolean isDigit(char c) {
            return Character.isDigit(c);
        }

        private boolean isHexDigit(char c) {
            return isDigit(c) ||
                    (c >= 'a' && c <= 'f') ||
                    (c >= 'A' && c <= 'F');
        }

        private boolean isIllegalHexLetter(char c) {
            return (c >= 'g' && c <= 'z') ||
                    (c >= 'G' && c <= 'Z');
        }

        private void skipWhitespace() {
            while (Character.isWhitespace(peek())) {
                pos++;
            }
        }

        public Token nextToken() {
            skipWhitespace();

            if (pos >= length) {
                return null;
            }

            char c = peek();

            // 标识符或关键字
            if (isLetter(c)) {
                StringBuilder sb = new StringBuilder();

                while (isLetter(peek()) || isDigit(peek())) {
                    sb.append(next());
                }

                String word = sb.toString();

                if (keywords.contains(word)) {
                    return new Token(word.toUpperCase(), word);
                } else {
                    return new Token("IDN", word);
                }
            }

            // 整数
            if (isDigit(c)) {
                return numberToken();
            }

            // 运算符和分隔符
            switch (c) {
                case '+':
                    next();
                    return new Token("ADD", "+");
                case '-':
                    next();
                    return new Token("SUB", "-");
                case '*':
                    next();
                    return new Token("MUL", "*");
                case '/':
                    next();
                    return new Token("DIV", "/");
                case '(':
                    next();
                    return new Token("SLP", "(");
                case ')':
                    next();
                    return new Token("SRP", ")");
                case ';':
                    next();
                    return new Token("SEMI", ";");
                case '=':
                    next();
                    return new Token("EQ", "=");
                case '>':
                    next();
                    if (peek() == '=') {
                        next();
                        return new Token("GE", ">=");
                    }
                    return new Token("GT", ">");
                case '<':
                    next();
                    if (peek() == '=') {
                        next();
                        return new Token("LE", "<=");
                    } else if (peek() == '>') {
                        next();
                        return new Token("NEQ", "<>");
                    }
                    return new Token("LT", "<");
                default:
                    next();
                    return new Token("UNKNOWN", String.valueOf(c));
            }
        }

        private Token numberToken() {
            StringBuilder sb = new StringBuilder();

            // 十进制整数
            if (peek() >= '1' && peek() <= '9') {
                while (isDigit(peek())) {
                    sb.append(next());
                }

                int value = Integer.parseInt(sb.toString());
                return new Token("DEC", String.valueOf(value));
            }

            // 以 0 开头
            sb.append(next());

            // 单独的 0
            if (!isDigit(peek()) && peek() != 'x' && peek() != 'X') {
                return new Token("DEC", "0");
            }

            // 十六进制
            if (peek() == 'x' || peek() == 'X') {
                sb.append(next());

                boolean hasDigitOrLetter = false;
                boolean illegal = false;

                while (isDigit(peek()) || isLetter(peek())) {
                    char ch = peek();

                    if (isIllegalHexLetter(ch)) {
                        illegal = true;
                    }

                    if (!isHexDigit(ch)) {
                        illegal = true;
                    }

                    hasDigitOrLetter = true;
                    sb.append(next());
                }

                if (!hasDigitOrLetter || illegal) {
                    return new Token("ILHEX", "-");
                } else {
                    String hexString = sb.substring(2);
                    int value = Integer.parseInt(hexString, 16);
                    return new Token("HEX", String.valueOf(value));
                }
            }

            // 八进制
            boolean illegalOct = false;

            while (isDigit(peek())) {
                char ch = peek();

                if (ch == '8' || ch == '9') {
                    illegalOct = true;
                }

                sb.append(next());
            }

            if (illegalOct) {
                return new Token("ILOCT", "-");
            } else {
                String octString = sb.toString();
                int value = Integer.parseInt(octString, 8);
                return new Token("OCT", String.valueOf(value));
            }
        }
    }

    /**
     * 实验二 + 实验三共用 Parser
     *
     * enableTreeOutput:
     * true  表示输出实验二语法树
     * false 表示不输出语法树
     *
     * enableCodeGen:
     * true  表示生成实验三三地址代码
     * false 表示只做语法分析
     */
    static class Parser {
        private final Lexer lexer;
        private Token lookahead;
        private int indent = 0;

        private final boolean enableTreeOutput;
        private final boolean enableCodeGen;
        private final CodeGenerator codeGen;

        static class ConditionJump {
            int falseGotoIndex;

            ConditionJump(int falseGotoIndex) {
                this.falseGotoIndex = falseGotoIndex;
            }
        }

        Parser(Lexer lexer, boolean enableTreeOutput, boolean enableCodeGen, CodeGenerator codeGen) {
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

        private void error(String message) {
            if (lookahead == null) {
                throw new RuntimeException("语法错误：" + message + "，当前已经到达文件结束");
            } else {
                throw new RuntimeException(
                        "语法错误：" + message +
                                "，当前单词为：" + lookahead.type +
                                "，属性值：" + lookahead.value
                );
            }
        }

        private boolean check(String type) {
            return lookahead != null && lookahead.type.equals(type);
        }

        private void match(String type) {
            if (check(type)) {
                printTree(tokenDisplay(lookahead));
                lookahead = lexer.nextToken();
            } else {
                String current = lookahead == null ? "EOF" : lookahead.type;
                error("期望 " + type + "，但遇到 " + current);
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
            }

            while (lookahead != null) {
                String nextLabel = null;

                if (enableCodeGen) {
                    nextLabel = codeGen.newProgramNextLabel();
                }

                parseL(nextLabel);

                if (enableCodeGen && (lookahead != null || codeGen.isLabelReferenced(nextLabel))) {
                    codeGen.emitLabel(nextLabel);
                }
            }

            indent--;

            if (enableTreeOutput) {
                System.out.println("语法分析成功！");
            }
        }

        /**
         * L -> S ;
         */
        private void parseL(String nextLabel) {
            printTree("L", "L -> S ;");
            indent++;

            parseS(nextLabel);
            match("SEMI");

            indent--;
        }

        /**
         * S -> id = E
         * S -> if C then S S'
         * S -> while C do S
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
            } else {
                error("无法识别的语句开头");
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

            if (enableCodeGen) {
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

                if (enableCodeGen) {
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

            if (enableCodeGen) {
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

                if (enableCodeGen) {
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

                if (enableCodeGen) {
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
                error("非法八进制整数");
                return null;

            } else if (check("ILHEX")) {
                error("非法十六进制整数");
                return null;

            } else {
                error("因子错误，期望 id、整数或括号表达式");
                return null;
            }
        }
    }

    /**
     * 实验二接口：只打印语法树，不生成三地址代码
     */
    public static void parseAndPrintTree(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer, true, false, null);
        parser.parseProgram();
    }

    /**
     * 实验三接口：生成三地址代码
     *
     * 这个接口是实验三真正有用的接口。
     * 它不返回 success/message，而是直接返回三地址代码。
     *
     * 如果语法错误，会抛出 RuntimeException。
     */
    public static List<String> generateCodeForExperiment3(String source) {
        Lexer lexer = new Lexer(source);
        CodeGenerator codeGen = new CodeGenerator();

        Parser parser = new Parser(lexer, false, true, codeGen);
        parser.parseProgram();

        return codeGen.getCodes();
    }

    private static String readSource() throws IOException {
        StringBuilder source = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while ((line = br.readLine()) != null) {
            source.append(line).append('\n');
        }

        return source.toString();
    }

    public static void main(String[] args) throws IOException {
        String source = readSource();

        try {
            String mode = args.length == 0 ? "--tac" : args[0];

            if (mode.equals("--tree")) {
                parseAndPrintTree(source);
            } else if (mode.equals("--tac")) {
                List<String> codes = generateCodeForExperiment3(source);

                for (String code : codes) {
                    System.out.println(code);
                }
            } else {
                throw new RuntimeException("未知运行模式：" + mode);
            }

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
