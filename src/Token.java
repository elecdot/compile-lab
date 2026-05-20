final class Token {
    final String type;
    final String value;
    final String lexeme;

    Token(String type, String value) {
        this(type, value, value);
    }

    Token(String type, String value, String lexeme) {
        this.type = type;
        this.value = value;
        this.lexeme = lexeme;
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
