import java.util.ArrayList;
import java.util.List;

final class TacAstPrinter {
    private final List<String> lines = new ArrayList<>();

    List<String> print(TacProgram program) {
        lines.clear();
        line(0, "Program");
        for (TacStatement statement : program.statements) {
            printStatement(statement, 1);
        }
        return new ArrayList<>(lines);
    }

    private void printStatement(TacStatement statement, int indent) {
        if (statement instanceof TacAssign) {
            TacAssign assign = (TacAssign) statement;
            line(indent, "Assign " + assign.id);
            printExpr(assign.expr, indent + 1);
        } else if (statement instanceof TacIf) {
            TacIf ifStatement = (TacIf) statement;
            line(indent, "If");
            printCondition(ifStatement.condition, indent + 1);
            line(indent + 1, "Then");
            printStatement(ifStatement.thenBranch, indent + 2);
            if (ifStatement.elseBranch != null) {
                line(indent + 1, "Else");
                printStatement(ifStatement.elseBranch, indent + 2);
            }
        } else if (statement instanceof TacWhile) {
            TacWhile whileStatement = (TacWhile) statement;
            line(indent, "While");
            printCondition(whileStatement.condition, indent + 1);
            printStatement(whileStatement.body, indent + 1);
        } else if (statement instanceof TacCompound) {
            TacCompound compound = (TacCompound) statement;
            line(indent, "Compound");
            for (TacStatement inner : compound.statements) {
                printStatement(inner, indent + 1);
            }
        } else if (statement instanceof TacError) {
            line(indent, "ErrorStatement");
        } else {
            line(indent, "UnknownStatement");
        }
    }

    private void printCondition(TacCondition condition, int indent) {
        line(indent, "Condition " + condition.op);
        printExpr(condition.left, indent + 1);
        printExpr(condition.right, indent + 1);
    }

    private void printExpr(TacExpr expr, int indent) {
        if (expr instanceof TacValue) {
            line(indent, "Value " + ((TacValue) expr).place);
        } else if (expr instanceof TacBinary) {
            TacBinary binary = (TacBinary) expr;
            line(indent, "Binary " + binary.op);
            printExpr(binary.left, indent + 1);
            printExpr(binary.right, indent + 1);
        } else {
            line(indent, "UnknownExpr");
        }
    }

    private void line(int indent, String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            builder.append("  ");
        }
        builder.append(text);
        lines.add(builder.toString());
    }
}

