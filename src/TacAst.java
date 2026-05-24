import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TacProgram {
    final List<TacStatement> statements;

    TacProgram(List<TacStatement> statements) {
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }
}

abstract class TacStatement {
}

final class TacAssign extends TacStatement {
    final String id;
    final TacExpr expr;

    TacAssign(String id, TacExpr expr) {
        this.id = id;
        this.expr = expr;
    }
}

final class TacIf extends TacStatement {
    final TacCondition condition;
    final TacStatement thenBranch;
    final TacStatement elseBranch;

    TacIf(TacCondition condition, TacStatement thenBranch, TacStatement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}

final class TacWhile extends TacStatement {
    final TacCondition condition;
    final TacStatement body;

    TacWhile(TacCondition condition, TacStatement body) {
        this.condition = condition;
        this.body = body;
    }
}

final class TacCompound extends TacStatement {
    final List<TacStatement> statements;

    TacCompound(List<TacStatement> statements) {
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }
}

abstract class TacExpr {
}

final class TacValue extends TacExpr {
    final String place;

    TacValue(String place) {
        this.place = place;
    }
}

final class TacBinary extends TacExpr {
    final TacExpr left;
    final String op;
    final TacExpr right;

    TacBinary(TacExpr left, String op, TacExpr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

final class TacCondition {
    final TacExpr left;
    final String op;
    final TacExpr right;

    TacCondition(TacExpr left, String op, TacExpr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

