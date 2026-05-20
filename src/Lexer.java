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

    Token nextToken() {
        skipWhitespace();

        if (pos >= length) {
            return null;
        }

        char c = peek();

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

        if (isDigit(c)) {
            return numberToken();
        }

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

        if (peek() >= '1' && peek() <= '9') {
            while (isDigit(peek())) {
                sb.append(next());
            }

            int value = Integer.parseInt(sb.toString());
            return new Token("DEC", String.valueOf(value));
        }

        sb.append(next());

        if (!isDigit(peek()) && peek() != 'x' && peek() != 'X') {
            return new Token("DEC", "0");
        }

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
