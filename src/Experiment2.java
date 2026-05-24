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

    /**
     * 实验三扩展接口：生成经过常量折叠的三地址代码。
     */
    public static List<String> generateOptimizedCodeForExperiment3(String source) {
        Lexer lexer = new Lexer(source);
        CodeGenerator codeGen = new CodeGenerator();
        BisonTacParser.ParseResult result = BisonTacParser.parseForExperiment3(lexer);

        for (String error : result.errorMessages) {
            codeGen.emit(error);
        }

        TacProgram optimized = new TacOptimizer().foldConstants(result.program);
        new TacEmitter(codeGen).emitProgram(optimized);

        return codeGen.getCodes();
    }

    /**
     * 实验二/三 Bison 路径的 AST 展示接口。
     *
     * 该接口服务报告和汇报展示，不替代实验三 TAC 输出。
     */
    public static List<String> printAstForExperiment3(String source) {
        Lexer lexer = new Lexer(source);
        TacProgram program = BisonTacParser.parseAst(lexer);
        return new TacAstPrinter().print(program);
    }

    /**
     * 实验二/三 Bison 路径的 Graphviz DOT AST 展示接口。
     */
    public static List<String> printAstDotForExperiment3(String source) {
        Lexer lexer = new Lexer(source);
        TacProgram program = BisonTacParser.parseAst(lexer);
        return new TacAstDotPrinter().print(program);
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
            String mode = args.length == 0 ? "--tac" : args[0];

            if (mode.equals("--tree")) {
                parseAndPrintTree(source);
            } else if (mode.equals("--tac")) {
                List<String> codes = generateCodeForExperiment3(source);

                for (String code : codes) {
                    System.out.println(code);
                }
            } else if (mode.equals("--tac-opt")) {
                List<String> codes = generateOptimizedCodeForExperiment3(source);

                for (String code : codes) {
                    System.out.println(code);
                }
            } else if (mode.equals("--ast")) {
                List<String> lines = printAstForExperiment3(source);

                for (String line : lines) {
                    System.out.println(line);
                }
            } else if (mode.equals("--ast-dot")) {
                List<String> lines = printAstDotForExperiment3(source);

                for (String line : lines) {
                    System.out.println(line);
                }
            } else {
                throw new RuntimeException("未知运行模式：" + mode);
            }

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
