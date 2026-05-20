public class Main {

    public static void main(String[] args) throws Exception {
        String input = readAllInput();   // 1. 一次性读完整输入
        Lexer lexer = new Lexer(input);  // 2. 输入结束后再开始分析

        StringBuilder output = new StringBuilder();
        Token token;

        while ((token = lexer.nextToken()) != null) {
            output.append(token.type).append(" ").append(token.lab1Attribute()).append('\n');
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

}
