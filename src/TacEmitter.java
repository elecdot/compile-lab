final class TacEmitter {
    private final CodeGenerator codeGen;

    TacEmitter(CodeGenerator codeGen) {
        this.codeGen = codeGen;
    }

    void emitProgram(TacProgram program) {
        for (int i = 0; i < program.statements.size(); i++) {
            TacStatement statement = program.statements.get(i);
            if (statement instanceof TacError) {
                continue;
            }

            String nextLabel = statementCanReferenceNextLabel(statement) ? codeGen.newProgramNextLabel() : null;
            emitStatement(statement, nextLabel, null);

            if (nextLabel != null && codeGen.isLabelReferenced(nextLabel)) {
                codeGen.emitLabel(nextLabel);
            }
        }
    }

    private boolean statementCanReferenceNextLabel(TacStatement statement) {
        if (statement instanceof TacIf || statement instanceof TacWhile) {
            return true;
        }

        if (statement instanceof TacCompound) {
            TacCompound compound = (TacCompound) statement;
            for (TacStatement inner : compound.statements) {
                if (statementCanReferenceNextLabel(inner)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void emitStatement(TacStatement statement, String nextLabel, String currentLabel) {
        if (statement instanceof TacAssign) {
            emitAssign((TacAssign) statement);
        } else if (statement instanceof TacIf) {
            emitIf((TacIf) statement, nextLabel);
        } else if (statement instanceof TacWhile) {
            emitWhile((TacWhile) statement, nextLabel, currentLabel);
        } else if (statement instanceof TacCompound) {
            emitCompound((TacCompound) statement, nextLabel, currentLabel);
        } else if (statement instanceof TacError) {
            return;
        } else {
            throw new RuntimeException("未知语句类型");
        }
    }

    private void emitAssign(TacAssign statement) {
        String place = emitExpr(statement.expr);
        codeGen.emit(statement.id + " = " + place);
    }

    private void emitIf(TacIf statement, String nextLabel) {
        String trueLabel = codeGen.newLabel();
        int falseGotoIndex = emitCondition(statement.condition, trueLabel, nextLabel);

        codeGen.emitLabel(trueLabel);
        emitStatement(statement.thenBranch, nextLabel, trueLabel);

        if (statement.elseBranch != null) {
            String falseLabel = codeGen.newLabel();
            codeGen.patchGoto(falseGotoIndex, falseLabel);
            codeGen.emitGoto(nextLabel);
            codeGen.emitLabel(falseLabel);
            emitStatement(statement.elseBranch, nextLabel, null);
        }
    }

    private void emitWhile(TacWhile statement, String nextLabel, String currentLabel) {
        String beginLabel = currentLabel;
        if (beginLabel == null) {
            beginLabel = codeGen.newLabel();
            codeGen.emitLabel(beginLabel);
        }

        String trueLabel = codeGen.newLabel();
        emitCondition(statement.condition, trueLabel, nextLabel);

        codeGen.emitLabel(trueLabel);
        emitStatement(statement.body, beginLabel, null);
        codeGen.emitGoto(beginLabel);
    }

    private void emitCompound(TacCompound statement, String nextLabel, String currentLabel) {
        for (TacStatement inner : statement.statements) {
            emitStatement(inner, nextLabel, currentLabel);
        }
    }

    private int emitCondition(TacCondition condition, String trueLabel, String falseLabel) {
        String left = emitExpr(condition.left);
        String right = emitExpr(condition.right);
        codeGen.markLabelReferenced(trueLabel);
        codeGen.emit("if " + left + " " + condition.op + " " + right + " goto " + trueLabel);
        return codeGen.emitGoto(falseLabel);
    }

    private String emitExpr(TacExpr expr) {
        if (expr instanceof TacValue) {
            return ((TacValue) expr).place;
        }

        if (!(expr instanceof TacBinary)) {
            throw new RuntimeException("未知表达式类型");
        }

        TacBinary binary = (TacBinary) expr;
        String left = emitExpr(binary.left);
        String right = emitExpr(binary.right);
        String temp = codeGen.newTemp();

        if (binary.op.equals("+") || binary.op.equals("-")) {
            String assignOp = left.startsWith("t") ? " = " : " := ";
            codeGen.emit(temp + assignOp + left + " " + binary.op + " " + right);
        } else {
            codeGen.emit(temp + " = " + left + " " + binary.op + " " + right);
        }

        return temp;
    }
}
