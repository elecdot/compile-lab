final class Token {
    final String type;
    final String value;

    Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "  " + value;
    }
}
