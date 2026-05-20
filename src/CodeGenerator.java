import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class CodeGenerator {
    private int tempCount = 1;
    private int labelCount = 1;
    private boolean usedProgramNextLabel = false;
    private final List<String> codes = new ArrayList<>();
    private final Set<String> referencedLabels = new HashSet<>();

    String newTemp() {
        return "t" + tempCount++;
    }

    String newLabel() {
        return "L" + labelCount++;
    }

    String newProgramNextLabel() {
        if (!usedProgramNextLabel) {
            usedProgramNextLabel = true;
            return "L0";
        }

        return newLabel();
    }

    int emit(String code) {
        codes.add(code);
        return codes.size() - 1;
    }

    void emitLabel(String label) {
        codes.add(label + ":");
    }

    int emitGoto(String label) {
        referencedLabels.add(label);
        return emit("goto " + label);
    }

    void markLabelReferenced(String label) {
        referencedLabels.add(label);
    }

    void patchGoto(int index, String label) {
        referencedLabels.add(label);
        codes.set(index, "goto " + label);
    }

    boolean isLabelReferenced(String label) {
        return referencedLabels.contains(label);
    }

    List<String> getCodes() {
        List<String> formatted = new ArrayList<>();

        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);

            if (code.endsWith(":") && i + 1 < codes.size() && !codes.get(i + 1).endsWith(":")) {
                formatted.add(code + " " + codes.get(i + 1));
                i++;
            } else {
                formatted.add(code);
            }
        }

        return formatted;
    }

    void printCodes() {
        for (String code : codes) {
            System.out.println(code);
        }
    }
}
