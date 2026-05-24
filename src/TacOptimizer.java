import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

final class TacOptimizer {
    TacProgram foldConstants(TacProgram program) {
        List<TacStatement> statements = new ArrayList<>();
        for (TacStatement statement : program.statements) {
            statements.add(foldStatement(statement));
        }
        return new TacProgram(statements);
    }

    private TacStatement foldStatement(TacStatement statement) {
        if (statement instanceof TacAssign) {
            TacAssign assign = (TacAssign) statement;
            return new TacAssign(assign.id, foldExpr(assign.expr));
        }

        if (statement instanceof TacIf) {
            TacIf ifStatement = (TacIf) statement;
            TacStatement elseBranch = ifStatement.elseBranch == null
                    ? null
                    : foldStatement(ifStatement.elseBranch);
            return new TacIf(
                    foldCondition(ifStatement.condition),
                    foldStatement(ifStatement.thenBranch),
                    elseBranch
            );
        }

        if (statement instanceof TacWhile) {
            TacWhile whileStatement = (TacWhile) statement;
            return new TacWhile(
                    foldCondition(whileStatement.condition),
                    foldStatement(whileStatement.body)
            );
        }

        if (statement instanceof TacCompound) {
            TacCompound compound = (TacCompound) statement;
            List<TacStatement> statements = new ArrayList<>();
            for (TacStatement inner : compound.statements) {
                statements.add(foldStatement(inner));
            }
            return new TacCompound(statements);
        }

        return statement;
    }

    private TacCondition foldCondition(TacCondition condition) {
        return new TacCondition(
                foldExpr(condition.left),
                condition.op,
                foldExpr(condition.right)
        );
    }

    private TacExpr foldExpr(TacExpr expr) {
        if (!(expr instanceof TacBinary)) {
            return expr;
        }

        TacBinary binary = (TacBinary) expr;
        TacExpr left = foldExpr(binary.left);
        TacExpr right = foldExpr(binary.right);

        if (left instanceof TacValue && right instanceof TacValue) {
            String folded = foldValues(((TacValue) left).place, binary.op, ((TacValue) right).place);
            if (folded != null) {
                return new TacValue(folded);
            }
        }

        return new TacBinary(left, binary.op, right);
    }

    private String foldValues(String left, String op, String right) {
        if (!isInteger(left) || !isInteger(right)) {
            return null;
        }

        BigInteger leftValue = new BigInteger(left);
        BigInteger rightValue = new BigInteger(right);

        switch (op) {
            case "+":
                return leftValue.add(rightValue).toString();
            case "-":
                return leftValue.subtract(rightValue).toString();
            case "*":
                return leftValue.multiply(rightValue).toString();
            case "/":
                if (rightValue.equals(BigInteger.ZERO)) {
                    return null;
                }
                return leftValue.divide(rightValue).toString();
            default:
                return null;
        }
    }

    private boolean isInteger(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        int start = value.charAt(0) == '-' ? 1 : 0;
        if (start == value.length()) {
            return false;
        }

        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}

