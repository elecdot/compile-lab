import java.util.ArrayList;
import java.util.List;

final class BisonTacParser implements Parser {
    private final Lexer lexer;
    private final CodeGenerator codeGen;

    BisonTacParser(Lexer lexer, CodeGenerator codeGen) {
        this.lexer = lexer;
        this.codeGen = codeGen;
    }

    @Override
    public void parseProgram() {
        TacLexerAdapter adapter = new TacLexerAdapter(lexer);
        TacBisonParser parser = new TacBisonParser(adapter);

        if (!parser.parse()) {
            throw new RuntimeException(adapter.errorMessage());
        }

        TacProgram program = parser.getResult();
        if (program == null) {
            throw new RuntimeException("语法错误：未生成实验三语法树");
        }

        for (String error : adapter.errorMessages()) {
            codeGen.emit(error);
        }

        new TacEmitter(codeGen).emitProgram(program);
    }

    private static final class TacLexerAdapter implements TacBisonParser.Lexer {
        private final Lexer lexer;
        private Token currentToken;
        private Object value;
        private String errorMessage = "语法错误";
        private final List<String> errorMessages = new ArrayList<>();

        TacLexerAdapter(Lexer lexer) {
            this.lexer = lexer;
        }

        @Override
        public Object getLVal() {
            return value;
        }

        @Override
        public int yylex() {
            currentToken = lexer.nextToken();
            value = currentToken;

            if (currentToken == null) {
                return EOF;
            }

            switch (currentToken.type) {
                case "IDN":
                    return IDN;
                case "DEC":
                    return DEC;
                case "OCT":
                    return OCT;
                case "HEX":
                    return HEX;
                case "IF":
                    return IF;
                case "THEN":
                    return THEN;
                case "ELSE":
                    return ELSE;
                case "WHILE":
                    return WHILE;
                case "DO":
                    return DO;
                case "BEGIN":
                    return BEGIN;
                case "END":
                    return END;
                case "ADD":
                    return ADD;
                case "SUB":
                    return SUB;
                case "MUL":
                    return MUL;
                case "DIV":
                    return DIV;
                case "EQ":
                    return EQ;
                case "GT":
                    return GT;
                case "LT":
                    return LT;
                case "GE":
                    return GE;
                case "LE":
                    return LE;
                case "NEQ":
                    return NEQ;
                case "SLP":
                    return SLP;
                case "SRP":
                    return SRP;
                case "SEMI":
                    return SEMI;
                case "ILOCT":
                    return ILOCT;
                case "ILHEX":
                    return ILHEX;
                case "ILNUM":
                    return ILNUM;
                case "UNKNOWN":
                    return UNKNOWN;
                default:
                    return UNKNOWN;
            }
        }

        @Override
        public void yyerror(String message) {
            if (currentToken == null) {
                errorMessage = "语法错误 [文件末尾]: " + message;
            } else {
                errorMessage = "语法错误 [行" + currentToken.line + "列" + currentToken.column + "]: "
                        + message + "（当前token: " + currentToken.type + " '" + currentToken.lexeme + "'）";
            }
            errorMessages.add(errorMessage);
        }

        String errorMessage() {
            return errorMessage;
        }

        List<String> errorMessages() {
            return errorMessages;
        }
    }
}
