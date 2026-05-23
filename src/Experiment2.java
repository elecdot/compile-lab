import java.io.*;
import java.util.List;

public class Experiment2 {

    /**
     * 实验二接口：只打印语法树，不生成三地址代码
     */
    public static void parseAndPrintTree(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = Parsers.forTree(lexer);
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

        Parser parser = Parsers.forTac(lexer, codeGen);
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
            String mode = args.length == 0 ? "--tree" : args[0];

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
