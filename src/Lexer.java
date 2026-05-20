import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class Lexer {
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

    Token nextToken() {
        skipWhitespace();

        if (pos >= length) {
            return null;
        }

        char c = input.charAt(pos);

        if (c == '>') {
            if (peekNext('=')) {
                pos += 2;
                return new Token("GE", ">=", ">=");
            }
            pos++;
            return new Token("GT", ">", ">");
        }

        if (c == '<') {
            if (peekNext('=')) {
                pos += 2;
                return new Token("LE", "<=", "<=");
            } else if (peekNext('>')) {
                pos += 2;
                return new Token("NEQ", "<>", "<>");
            }
            pos++;
            return new Token("LT", "<", "<");
        }

        Token single = singleCharacterToken(c);
        if (single != null) {
            pos++;
            return single;
        }

        int start = pos;
        while (pos < length) {
            char t = input.charAt(pos);
            if (isWhitespace(t) || isOperatorOrDelimiterStart(t)) {
                break;
            }
            pos++;
        }

        String lexeme = input.substring(start, pos);
        return classifyLexeme(lexeme);
    }

    private Token singleCharacterToken(char c) {
        switch (c) {
            case '+':
                return new Token("ADD", "+", "+");
            case '-':
                return new Token("SUB", "-", "-");
            case '*':
                return new Token("MUL", "*", "*");
            case '/':
                return new Token("DIV", "/", "/");
            case '=':
                return new Token("EQ", "=", "=");
            case '(':
                return new Token("SLP", "(", "(");
            case ')':
                return new Token("SRP", ")", ")");
            case ';':
                return new Token("SEMI", ";", ";");
            default:
                return null;
        }
    }

    private Token classifyLexeme(String lexeme) {
        if (lexeme == null || lexeme.length() == 0) {
            return null;
        }

        String keyword = keywordType(lexeme);
        if (keyword != null) {
            return new Token(keyword, lexeme, lexeme);
        }

        if (isIdentifier(lexeme)) {
            return new Token("IDN", lexeme, lexeme);
        }

        if (isDigit(lexeme.charAt(0))) {
            return classifyNumber(lexeme);
        }

        return new Token("UNKNOWN", lexeme, lexeme);
    }

    private Token classifyNumber(String lexeme) {
        if (lexeme.equals("0")) {
            return new Token("DEC", "0", lexeme);
        }

        if (lexeme.length() >= 2 && lexeme.charAt(0) == '0' && (lexeme.charAt(1) == 'x' || lexeme.charAt(1) == 'X')) {
            if (lexeme.length() == 2) {
                return new Token("ILHEX", "-", lexeme);
            }

            boolean allHex = true;
            boolean hasIllegal = false;

            for (int i = 2; i < lexeme.length(); i++) {
                char ch = lexeme.charAt(i);
                if (isHexDigit(ch)) {
                    continue;
                }
                allHex = false;
                hasIllegal = true;
            }

            if (allHex) {
                return new Token("HEX", convertBaseToDecimal(lexeme.substring(2), 16), lexeme);
            }

            if (hasIllegal) {
                return new Token("ILHEX", "-", lexeme);
            }
        }

        if (lexeme.length() >= 2 && lexeme.charAt(0) == '0') {
            boolean allDigits = true;
            boolean has89 = false;
            boolean allOct = true;

            for (int i = 1; i < lexeme.length(); i++) {
                char ch = lexeme.charAt(i);
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
                return new Token("ILOCT", "-", lexeme);
            }

            if (allDigits && allOct) {
                return new Token("OCT", convertBaseToDecimal(lexeme.substring(1), 8), lexeme);
            }

            return new Token("ILNUM", "-", lexeme);
        }

        if (isDecimal(lexeme)) {
            return new Token("DEC", lexeme, lexeme);
        }

        return new Token("ILNUM", "-", lexeme);
    }

    private void skipWhitespace() {
        while (pos < length && isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private boolean peekNext(char expected) {
        return pos + 1 < length && input.charAt(pos + 1) == expected;
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

    private boolean isIdentifier(String text) {
        if (text.length() == 0) return false;

        char first = text.charAt(0);
        if (!(Character.isLetter(first) || first == '_')) {
            return false;
        }

        for (int i = 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }
        }

        return true;
    }

    private boolean isDecimal(String text) {
        if (text.length() == 0) return false;
        if (text.equals("0")) return true;

        if (text.charAt(0) < '1' || text.charAt(0) > '9') {
            return false;
        }

        for (int i = 1; i < text.length(); i++) {
            if (!isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String keywordType(String text) {
        if (keywords.contains(text)) {
            return text.toUpperCase();
        }
        return null;
    }

    private String convertBaseToDecimal(String text, int base) {
        String result = "0";
        for (int i = 0; i < text.length(); i++) {
            int digit = digitValue(text.charAt(i));
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

    private String trimLeadingZeros(String text) {
        int i = 0;
        while (i < text.length() - 1 && text.charAt(i) == '0') {
            i++;
        }
        return text.substring(i);
    }
}
