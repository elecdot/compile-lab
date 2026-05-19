public class Main {

    public static void main(String[] args) throws Exception {
        String input = readAllInput();   // 1. 一次性读完整输入
        Lexer lexer = new Lexer(input);  // 2. 输入结束后再开始分析

        StringBuilder output = new StringBuilder();
        Token token;

        while ((token = lexer.nextToken()) != null) {
            output.append(token.type).append(" ").append(token.attr).append('\n');
        }

        System.out.print(output.toString());
    }

    // 一次性读取所有输入，直到 Ctrl+D / EOF
    private static String readAllInput() throws Exception {
        StringBuilder sb = new StringBuilder();
        java.io.InputStreamReader reader =
                new java.io.InputStreamReader(System.in, "UTF-8");

        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    static class Token {
        String type;
        String attr;

        Token(String type, String attr) {
            this.type = type;
            this.attr = attr;
        }
    }

    static class Lexer {
        private final String src;
        private final int len;
        private int pos;

        Lexer(String src) {
            this.src = src;
            this.len = src.length();
            this.pos = 0;
        }

        public Token nextToken() {
            skipWhitespace();
            if (pos >= len) {
                return null;
            }

            char c = src.charAt(pos);

            // 双字符运算符优先
            if (c == '>') {
                if (pos + 1 < len && src.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new Token("GE", "-");
                } else {
                    pos++;
                    return new Token("GT", "-");
                }
            }

            if (c == '<') {
                if (pos + 1 < len && src.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new Token("LE", "-");
                } else if (pos + 1 < len && src.charAt(pos + 1) == '>') {
                    pos += 2;
                    return new Token("NEQ", "-");
                } else {
                    pos++;
                    return new Token("LT", "-");
                }
            }

            // 单字符运算符和分隔符
            if (c == '+') {
                pos++;
                return new Token("ADD", "-");
            }
            if (c == '-') {
                pos++;
                return new Token("SUB", "-");
            }
            if (c == '*') {
                pos++;
                return new Token("MUL", "-");
            }
            if (c == '/') {
                pos++;
                return new Token("DIV", "-");
            }
            if (c == '=') {
                pos++;
                return new Token("EQ", "-");
            }
            if (c == '(') {
                pos++;
                return new Token("SLP", "-");
            }
            if (c == ')') {
                pos++;
                return new Token("SRP", "-");
            }
            if (c == ';') {
                pos++;
                return new Token("SEMI", "-");
            }

            // 普通串：一直读到空白或运算符/分隔符边界
            int start = pos;
            while (pos < len) {
                char t = src.charAt(pos);
                if (isWhitespace(t) || isOperatorOrDelimiterStart(t)) {
                    break;
                }
                pos++;
            }

            String lexeme = src.substring(start, pos);
            return classifyLexeme(lexeme);
        }

        private Token classifyLexeme(String s) {
            if (s == null || s.length() == 0) {
                return null;
            }

            // 关键字
            String kw = keywordType(s);
            if (kw != null) {
                return new Token(kw, "-");
            }

            // 标识符
            if (isIdentifier(s)) {
                return new Token("IDN", s);
            }

            // 数字
            if (isDigit(s.charAt(0))) {
                return classifyNumber(s);
            }

            // 其他未知串
            return new Token("UNKNOWN", s);
        }

        private Token classifyNumber(String s) {
            // 0
            if (s.equals("0")) {
                return new Token("DEC", "0");
            }

            // 0x / 0X 开头：十六进制
            if (s.length() >= 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                if (s.length() == 2) {
                    return new Token("ILHEX", "-");
                }

                boolean allHex = true;
                boolean hasIllegalLetter = false;
                boolean hasOtherIllegal = false;

                for (int i = 2; i < s.length(); i++) {
                    char ch = s.charAt(i);
                    if (isHexDigit(ch)) {
                        continue;
                    } else if (isAsciiLetter(ch)) {
                        hasIllegalLetter = true;
                        allHex = false;
                    } else {
                        hasOtherIllegal = true;
                        allHex = false;
                    }
                }

                if (allHex) {
                    String hexPart = s.substring(2);
                    return new Token("HEX", convertBaseToDecimal(hexPart, 16));
                }

                if (hasIllegalLetter || hasOtherIllegal) {
                    return new Token("ILHEX", "-");
                }
            }

            // 0开头：八进制或非法八进制
            if (s.length() >= 2 && s.charAt(0) == '0') {
                boolean allDigits = true;
                boolean has89 = false;
                boolean allOct = true;

                for (int i = 1; i < s.length(); i++) {
                    char ch = s.charAt(i);
                    if (!isDigit(ch)) {
                        allDigits = false;
                        allOct = false;
                        break;
                    }
                    if (ch == '8' || ch == '9') {
                        has89 = true;
                        allOct = false;
                    }
                    if (ch < '0' || ch > '7') {
                        allOct = false;
                    }
                }

                if (allDigits && has89) {
                    return new Token("ILOCT", "-");
                }

                if (allDigits && allOct) {
                    String octPart = s.substring(1);
                    return new Token("OCT", convertBaseToDecimal(octPart, 8));
                }

                return new Token("ILNUM", "-");
            }

            // 十进制
            if (isDecimal(s)) {
                return new Token("DEC", s);
            }

            // 例如 1f
            return new Token("ILNUM", "-");
        }

        private void skipWhitespace() {
            while (pos < len && isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private boolean isWhitespace(char c) {
            return c == ' ' || c == '\t' || c == '\n' || c == '\r';
        }

        private boolean isOperatorOrDelimiterStart(char c) {
            return c == '+' || c == '-' || c == '*' || c == '/' ||
                    c == '>' || c == '<' || c == '=' ||
                    c == '(' || c == ')' || c == ';';
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private boolean isAsciiLetter(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }

        private boolean isHexDigit(char c) {
            return (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'f') ||
                    (c >= 'A' && c <= 'F');
        }

        private boolean isIdentifier(String s) {
            if (s.length() == 0) return false;

            char first = s.charAt(0);
            if (!(Character.isLetter(first) || first == '_')) {
                return false;
            }

            for (int i = 1; i < s.length(); i++) {
                char c = s.charAt(i);
                if (!(Character.isLetterOrDigit(c) || c == '_')) {
                    return false;
                }
            }

            return true;
        }

        private boolean isDecimal(String s) {
            if (s.length() == 0) return false;
            if (s.equals("0")) return true;

            if (s.charAt(0) < '1' || s.charAt(0) > '9') {
                return false;
            }

            for (int i = 1; i < s.length(); i++) {
                if (!isDigit(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        private String keywordType(String s) {
            if (s.equals("if")) return "IF";
            if (s.equals("then")) return "THEN";
            if (s.equals("else")) return "ELSE";
            if (s.equals("while")) return "WHILE";
            if (s.equals("do")) return "DO";
            if (s.equals("begin")) return "BEGIN";
            if (s.equals("end")) return "END";
            return null;
        }

        private String convertBaseToDecimal(String s, int base) {
            String result = "0";
            for (int i = 0; i < s.length(); i++) {
                int digit = digitValue(s.charAt(i));
                result = multiplyAndAdd(result, base, digit);
            }
            return trimLeadingZeros(result);
        }

        private int digitValue(char c) {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            return 0;
        }

        private String multiplyAndAdd(String dec, int base, int add) {
            StringBuilder rev = new StringBuilder();
            int carry = add;

            for (int i = dec.length() - 1; i >= 0; i--) {
                int d = dec.charAt(i) - '0';
                int value = d * base + carry;
                rev.append((char) ('0' + (value % 10)));
                carry = value / 10;
            }

            while (carry > 0) {
                rev.append((char) ('0' + (carry % 10)));
                carry /= 10;
            }

            return rev.reverse().toString();
        }

        private String trimLeadingZeros(String s) {
            int i = 0;
            while (i < s.length() - 1 && s.charAt(i) == '0') {
                i++;
            }
            return s.substring(i);
        }
    }
}