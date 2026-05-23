final class Token {
    final String type;
    final String value;
    final String lexeme;
    final int line;
    final int column;

    Token(String type, String value) {
        this(type, value, value, 0, 0);
    }

    Token(String type, String value, String lexeme) {
        this(type, value, lexeme, 0, 0);
    }

    Token(String type, String value, String lexeme, int line, int column) {
        this.type = type;
        this.value = value;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    String lab1Attribute() {
        if (type.equals("IDN") || type.equals("DEC") || type.equals("OCT") || type.equals("HEX")) {
            return value;
        }

        if (type.equals("UNKNOWN")) {
            return lexeme;
        }

        return "-";
    }

    @Override
    public String toString() {
        return type + "  " + value;
    }
}
