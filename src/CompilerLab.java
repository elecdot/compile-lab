import java.util.Arrays;

public class CompilerLab {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("help") || args[0].equals("--help") || args[0].equals("-h")) {
            printUsage();
            return;
        }

        String command = args[0];
        String[] rest = Arrays.copyOfRange(args, 1, args.length);

        if (command.equals("lab1") || command.equals("lexer")) {
            Main.main(rest);
        } else if (command.equals("lab2") || command.equals("tree") || command.equals("lab2-tree")) {
            Experiment2.main(new String[]{"--tree"});
        } else if (command.equals("lab3") || command.equals("tac") || command.equals("lab3-tac")) {
            Experiment2.main(new String[]{"--tac"});
        } else if (command.equals("tac-opt") || command.equals("lab3-tac-opt")) {
            Experiment2.main(new String[]{"--tac-opt"});
        } else if (command.equals("ast")) {
            Experiment2.main(new String[]{"--ast"});
        } else if (command.equals("ast-dot")) {
            Experiment2.main(new String[]{"--ast-dot"});
        } else if (command.equals("minislr")) {
            MiniSlrDemo.main(new String[0]);
        } else if (command.equals("minislr-dot")) {
            MiniSlrDemo.main(new String[]{"--dot"});
        } else {
            System.err.println("未知命令：" + command);
            printUsage();
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar dist/compiler-lab.jar <command> < input");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  lab1         Run Lab 1 lexer output");
        System.out.println("  tree         Run Lab 2 recursive-descent syntax tree output");
        System.out.println("  tac          Run Lab 3 three-address-code output");
        System.out.println("  tac-opt      Run Lab 3 optimized TAC output");
        System.out.println("  ast          Print Bison-path AST text");
        System.out.println("  ast-dot      Print Bison-path AST Graphviz DOT");
        System.out.println("  minislr      Print MiniYacc/SLR table demo");
        System.out.println("  minislr-dot  Print MiniYacc/SLR automaton DOT");
    }
}
