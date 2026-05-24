import java.util.ArrayList;
import java.util.List;

final class TacAstDotPrinter {
    private final List<String> lines = new ArrayList<>();
    private int nextNodeId;

    List<String> print(TacProgram program) {
        lines.clear();
        nextNodeId = 0;

        lines.add("digraph AST {");
        lines.add("  node [shape=box];");

        int root = node("Program");
        for (TacStatement statement : program.statements) {
            printStatement(root, statement);
        }

        lines.add("}");
        return new ArrayList<>(lines);
    }

    private int printStatement(int parent, TacStatement statement) {
        if (statement instanceof TacAssign) {
            TacAssign assign = (TacAssign) statement;
            int id = node("Assign " + assign.id);
            edge(parent, id);
            printExpr(id, assign.expr);
            return id;
        }

        if (statement instanceof TacIf) {
            TacIf ifStatement = (TacIf) statement;
            int id = node("If");
            edge(parent, id);
            printCondition(id, ifStatement.condition);

            int thenNode = node("Then");
            edge(id, thenNode);
            printStatement(thenNode, ifStatement.thenBranch);

            if (ifStatement.elseBranch != null) {
                int elseNode = node("Else");
                edge(id, elseNode);
                printStatement(elseNode, ifStatement.elseBranch);
            }
            return id;
        }

        if (statement instanceof TacWhile) {
            TacWhile whileStatement = (TacWhile) statement;
            int id = node("While");
            edge(parent, id);
            printCondition(id, whileStatement.condition);
            printStatement(id, whileStatement.body);
            return id;
        }

        if (statement instanceof TacCompound) {
            TacCompound compound = (TacCompound) statement;
            int id = node("Compound");
            edge(parent, id);
            for (TacStatement inner : compound.statements) {
                printStatement(id, inner);
            }
            return id;
        }

        if (statement instanceof TacError) {
            int id = node("ErrorStatement");
            edge(parent, id);
            return id;
        }

        int id = node("UnknownStatement");
        edge(parent, id);
        return id;
    }

    private int printCondition(int parent, TacCondition condition) {
        int id = node("Condition " + condition.op);
        edge(parent, id);
        printExpr(id, condition.left);
        printExpr(id, condition.right);
        return id;
    }

    private int printExpr(int parent, TacExpr expr) {
        if (expr instanceof TacValue) {
            int id = node("Value " + ((TacValue) expr).place);
            edge(parent, id);
            return id;
        }

        if (expr instanceof TacBinary) {
            TacBinary binary = (TacBinary) expr;
            int id = node("Binary " + binary.op);
            edge(parent, id);
            printExpr(id, binary.left);
            printExpr(id, binary.right);
            return id;
        }

        int id = node("UnknownExpr");
        edge(parent, id);
        return id;
    }

    private int node(String label) {
        int id = nextNodeId++;
        lines.add("  n" + id + " [label=\"" + escape(label) + "\"];");
        return id;
    }

    private void edge(int from, int to) {
        lines.add("  n" + from + " -> n" + to + ";");
    }

    private String escape(String label) {
        return label.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
