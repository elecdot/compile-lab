import java.io.*;
import java.util.*;

public class Experiment2 {

    static class Token {
        String type;
        String value;

        Token(String type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return type + "  " + value;
        }
    }

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

            // 识别标识符或关键字
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

            // 识别整数
            if (isDigit(c)) {
                return numberToken();
            }

            // 识别运算符和分隔符
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
     * 实验二：递归下降语法分析器
     *
     * 消除左递归后的文法：
     *
     * P  -> L P | L
     * L  -> S ;
     *
     * S  -> id = E
     * S  -> if C then S
     * S  -> if C then S else S
     * S  -> while C do S
     *
     * C  -> E relop E
     *
     * E  -> T E'
     * E' -> + T E' | - T E' | ε
     *
     * T  -> F T'
     * T' -> * F T' | / F T' | ε
     *
     * F  -> ( E ) | id | int8 | int10 | int16
     */
    static class Parser {
        private final Lexer lexer;
        private Token lookahead;

        Parser(Lexer lexer) {
            this.lexer = lexer;
            this.lookahead = lexer.nextToken();
        }

        private void printProduction(String production) {
            System.out.println(production);
        }

        private void error(String message) {
            if (lookahead == null) {
                throw new RuntimeException("语法错误：" + message + "，当前已经到达文件结束");
            } else {
                throw new RuntimeException(
                        "语法错误：" + message + "，当前单词为：" + lookahead.type + "，属性值：" + lookahead.value
                );
            }
        }

        private boolean check(String type) {
            return lookahead != null && lookahead.type.equals(type);
        }

        private void match(String type) {
            if (check(type)) {
                lookahead = lexer.nextToken();
            } else {
                String current = lookahead == null ? "EOF" : lookahead.type;
                error("期望 " + type + "，但遇到 " + current);
            }
        }

        /**
         * P -> L P | L
         *
         * 源程序可以包含多个语句，所以这里用 while 循环反复分析 L。
         */
        public void parseProgram() {
            printProduction("P -> L P | L");

            while (lookahead != null) {
                parseL();
            }

            System.out.println("语法分析成功！");
        }

        /**
         * L -> S ;
         */
        private void parseL() {
            printProduction("L -> S ;");
            parseS();
            match("SEMI");
        }

        /**
         * S -> id = E
         * S -> if C then S
         * S -> if C then S else S
         * S -> while C do S
         */
        private void parseS() {
            if (check("IDN")) {
                printProduction("S -> id = E");
                match("IDN");
                match("EQ");
                parseE();
            } else if (check("IF")) {
                parseIf();
            } else if (check("WHILE")) {
                parseWhile();
            } else {
                error("无法识别的语句开头");
            }
        }

        /**
         * if 语句
         *
         * S -> if C then S
         * S -> if C then S else S
         */
        private void parseIf() {
            match("IF");
            parseC();
            match("THEN");
            parseS();

            if (check("ELSE")) {
                printProduction("S -> if C then S else S");
                match("ELSE");
                parseS();
            } else {
                printProduction("S -> if C then S");
            }
        }

        /**
         * while 语句
         *
         * S -> while C do S
         */
        private void parseWhile() {
            printProduction("S -> while C do S");
            match("WHILE");
            parseC();
            match("DO");
            parseS();
        }

        /**
         * C -> E > E
         * C -> E < E
         * C -> E = E
         */
        private void parseC() {
            printProduction("C -> E relop E");
            parseE();

            if (check("GT")) {
                printProduction("relop -> >");
                match("GT");
            } else if (check("LT")) {
                printProduction("relop -> <");
                match("LT");
            } else if (check("EQ")) {
                printProduction("relop -> =");
                match("EQ");
            } else {
                error("条件表达式中缺少关系运算符 >、< 或 =");
            }

            parseE();
        }

        /**
         * E -> T E'
         */
        private void parseE() {
            printProduction("E -> T E'");
            parseT();
            parseEPrime();
        }

        /**
         * E' -> + T E'
         * E' -> - T E'
         * E' -> ε
         */
        private void parseEPrime() {
            if (check("ADD")) {
                printProduction("E' -> + T E'");
                match("ADD");
                parseT();
                parseEPrime();
            } else if (check("SUB")) {
                printProduction("E' -> - T E'");
                match("SUB");
                parseT();
                parseEPrime();
            } else {
                printProduction("E' -> ε");
            }
        }

        /**
         * T -> F T'
         */
        private void parseT() {
            printProduction("T -> F T'");
            parseF();
            parseTPrime();
        }

        /**
         * T' -> * F T'
         * T' -> / F T'
         * T' -> ε
         */
        private void parseTPrime() {
            if (check("MUL")) {
                printProduction("T' -> * F T'");
                match("MUL");
                parseF();
                parseTPrime();
            } else if (check("DIV")) {
                printProduction("T' -> / F T'");
                match("DIV");
                parseF();
                parseTPrime();
            } else {
                printProduction("T' -> ε");
            }
        }

        /**
         * F -> ( E )
         * F -> id
         * F -> int8
         * F -> int10
         * F -> int16
         */
        private void parseF() {
            if (check("SLP")) {
                printProduction("F -> ( E )");
                match("SLP");
                parseE();
                match("SRP");
            } else if (check("IDN")) {
                printProduction("F -> id");
                match("IDN");
            } else if (check("OCT")) {
                printProduction("F -> int8");
                match("OCT");
            } else if (check("DEC")) {
                printProduction("F -> int10");
                match("DEC");
            } else if (check("HEX")) {
                printProduction("F -> int16");
                match("HEX");
            } else if (check("ILOCT")) {
                error("非法八进制整数");
            } else if (check("ILHEX")) {
                error("非法十六进制整数");
            } else {
                error("因子错误，期望 id、整数或括号表达式");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        StringBuilder source = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while ((line = br.readLine()) != null) {
            source.append(line).append('\n');
        }

        Lexer lexer = new Lexer(source.toString());
        Parser parser = new Parser(lexer);

        try {
            parser.parseProgram();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}