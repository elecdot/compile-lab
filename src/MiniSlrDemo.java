import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class MiniSlrDemo {
    private final List<Production> productions = new ArrayList<>();
    private final List<String> terminals = Arrays.asList("id", "+", "*", "(", ")", "$");
    private final List<String> nonterminals = Arrays.asList("E", "T", "F");
    private final List<String> gotoSymbols = Arrays.asList("E", "T", "F", "(", "id", "+", "*", ")");
    private final Map<String, List<Production>> productionsByLhs = new LinkedHashMap<>();
    private final Map<String, Set<String>> follow = new LinkedHashMap<>();

    public static void main(String[] args) {
        MiniSlrDemo demo = new MiniSlrDemo();
        for (String line : demo.render()) {
            System.out.println(line);
        }
    }

    private MiniSlrDemo() {
        add(0, "S'", "E");
        add(1, "E", "E", "+", "T");
        add(2, "E", "T");
        add(3, "T", "T", "*", "F");
        add(4, "T", "F");
        add(5, "F", "(", "E", ")");
        add(6, "F", "id");

        for (Production production : productions) {
            productionsByLhs
                    .computeIfAbsent(production.lhs, key -> new ArrayList<>())
                    .add(production);
        }

        computeFollow();
    }

    private List<String> render() {
        Automaton automaton = buildAutomaton();
        Table table = buildTable(automaton);
        List<String> lines = new ArrayList<>();

        lines.add("MiniYacc SLR(1) Demo");
        lines.add("");
        lines.add("Grammar:");
        for (Production production : productions) {
            lines.add("  (" + production.index + ") " + production);
        }

        lines.add("");
        lines.add("Canonical LR(0) item sets:");
        for (int i = 0; i < automaton.states.size(); i++) {
            lines.add("I" + i + ":");
            for (Item item : automaton.states.get(i)) {
                lines.add("  " + item);
            }
        }

        lines.add("");
        lines.add("GOTO transitions:");
        for (Transition transition : automaton.transitions) {
            lines.add("  I" + transition.from + " -- " + transition.symbol + " --> I" + transition.to);
        }

        lines.add("");
        lines.add("ACTION/GOTO table:");
        lines.add("State | id | + | * | ( | ) | $ | E | T | F");
        for (int state = 0; state < automaton.states.size(); state++) {
            List<String> cells = new ArrayList<>();
            cells.add(Integer.toString(state));
            for (String terminal : terminals) {
                cells.add(table.action(state, terminal));
            }
            for (String nonterminal : nonterminals) {
                cells.add(table.goTo(state, nonterminal));
            }
            lines.add(joinCells(cells));
        }

        return lines;
    }

    private Automaton buildAutomaton() {
        List<Set<Item>> states = new ArrayList<>();
        List<Transition> transitions = new ArrayList<>();
        Queue<Integer> worklist = new ArrayDeque<>();

        Set<Item> start = closure(Collections.singleton(new Item(productions.get(0), 0)));
        states.add(start);
        worklist.add(0);

        while (!worklist.isEmpty()) {
            int from = worklist.remove();
            Set<Item> state = states.get(from);

            for (String symbol : gotoSymbols) {
                Set<Item> target = goTo(state, symbol);
                if (target.isEmpty()) {
                    continue;
                }

                int to = indexOfState(states, target);
                if (to < 0) {
                    to = states.size();
                    states.add(target);
                    worklist.add(to);
                }
                transitions.add(new Transition(from, symbol, to));
            }
        }

        return new Automaton(states, transitions);
    }

    private Table buildTable(Automaton automaton) {
        Table table = new Table();

        for (int stateIndex = 0; stateIndex < automaton.states.size(); stateIndex++) {
            Set<Item> state = automaton.states.get(stateIndex);

            for (Item item : state) {
                String symbol = item.symbolAfterDot();
                if (symbol != null && isTerminal(symbol)) {
                    int target = transitionTarget(automaton, stateIndex, symbol);
                    table.putAction(stateIndex, symbol, "s" + target);
                } else if (item.isComplete()) {
                    if (item.production.index == 0) {
                        table.putAction(stateIndex, "$", "acc");
                    } else {
                        for (String terminal : follow.get(item.production.lhs)) {
                            table.putAction(stateIndex, terminal, "r" + item.production.index);
                        }
                    }
                }
            }

            for (String nonterminal : nonterminals) {
                int target = transitionTarget(automaton, stateIndex, nonterminal);
                if (target >= 0) {
                    table.putGoto(stateIndex, nonterminal, Integer.toString(target));
                }
            }
        }

        return table;
    }

    private Set<Item> closure(Set<Item> seed) {
        LinkedHashSet<Item> result = new LinkedHashSet<>(seed);
        boolean changed = true;

        while (changed) {
            changed = false;
            List<Item> snapshot = new ArrayList<>(result);
            for (Item item : snapshot) {
                String symbol = item.symbolAfterDot();
                if (symbol == null || !isNonterminal(symbol)) {
                    continue;
                }

                for (Production production : productionsByLhs.get(symbol)) {
                    if (result.add(new Item(production, 0))) {
                        changed = true;
                    }
                }
            }
        }

        return result;
    }

    private Set<Item> goTo(Set<Item> state, String symbol) {
        LinkedHashSet<Item> moved = new LinkedHashSet<>();
        for (Item item : state) {
            if (symbol.equals(item.symbolAfterDot())) {
                moved.add(new Item(item.production, item.dot + 1));
            }
        }
        return moved.isEmpty() ? moved : closure(moved);
    }

    private void computeFollow() {
        for (String nonterminal : nonterminals) {
            follow.put(nonterminal, new LinkedHashSet<>());
        }
        follow.get("E").add("$");

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production production : productions) {
                List<String> rhs = production.rhs;
                for (int i = 0; i < rhs.size(); i++) {
                    String symbol = rhs.get(i);
                    if (!isNonterminal(symbol)) {
                        continue;
                    }

                    Set<String> target = follow.get(symbol);
                    int before = target.size();

                    if (i + 1 < rhs.size()) {
                        String next = rhs.get(i + 1);
                        if (isTerminal(next)) {
                            target.add(next);
                        } else {
                            target.addAll(first(next));
                        }
                    } else if (follow.containsKey(production.lhs)) {
                        target.addAll(follow.get(production.lhs));
                    }

                    if (target.size() != before) {
                        changed = true;
                    }
                }
            }
        }
    }

    private Set<String> first(String nonterminal) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Production production : productionsByLhs.get(nonterminal)) {
            String first = production.rhs.get(0);
            if (isTerminal(first)) {
                result.add(first);
            } else {
                result.addAll(first(first));
            }
        }
        return result;
    }

    private int transitionTarget(Automaton automaton, int from, String symbol) {
        for (Transition transition : automaton.transitions) {
            if (transition.from == from && transition.symbol.equals(symbol)) {
                return transition.to;
            }
        }
        return -1;
    }

    private int indexOfState(List<Set<Item>> states, Set<Item> target) {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private void add(int index, String lhs, String... rhs) {
        productions.add(new Production(index, lhs, Arrays.asList(rhs)));
    }

    private boolean isTerminal(String symbol) {
        return terminals.contains(symbol) && !symbol.equals("$");
    }

    private boolean isNonterminal(String symbol) {
        return nonterminals.contains(symbol);
    }

    private String joinCells(List<String> cells) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(cells.get(i));
        }
        return builder.toString();
    }

    private static final class Production {
        final int index;
        final String lhs;
        final List<String> rhs;

        Production(int index, String lhs, List<String> rhs) {
            this.index = index;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String toString() {
            return lhs + " -> " + String.join(" ", rhs);
        }
    }

    private static final class Item {
        final Production production;
        final int dot;

        Item(Production production, int dot) {
            this.production = production;
            this.dot = dot;
        }

        boolean isComplete() {
            return dot >= production.rhs.size();
        }

        String symbolAfterDot() {
            return isComplete() ? null : production.rhs.get(dot);
        }

        @Override
        public String toString() {
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < production.rhs.size(); i++) {
                if (i == dot) {
                    parts.add(".");
                }
                parts.add(production.rhs.get(i));
            }
            if (isComplete()) {
                parts.add(".");
            }
            return production.lhs + " -> " + String.join(" ", parts);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Item)) {
                return false;
            }
            Item item = (Item) other;
            return production.index == item.production.index && dot == item.dot;
        }

        @Override
        public int hashCode() {
            return Objects.hash(production.index, dot);
        }
    }

    private static final class Transition {
        final int from;
        final String symbol;
        final int to;

        Transition(int from, String symbol, int to) {
            this.from = from;
            this.symbol = symbol;
            this.to = to;
        }
    }

    private static final class Automaton {
        final List<Set<Item>> states;
        final List<Transition> transitions;

        Automaton(List<Set<Item>> states, List<Transition> transitions) {
            this.states = states;
            this.transitions = transitions;
        }
    }

    private static final class Table {
        private final Map<String, String> action = new HashMap<>();
        private final Map<String, String> goTo = new HashMap<>();

        void putAction(int state, String terminal, String value) {
            action.put(key(state, terminal), value);
        }

        void putGoto(int state, String nonterminal, String value) {
            goTo.put(key(state, nonterminal), value);
        }

        String action(int state, String terminal) {
            return action.getOrDefault(key(state, terminal), "");
        }

        String goTo(int state, String nonterminal) {
            return goTo.getOrDefault(key(state, nonterminal), "");
        }

        private String key(int state, String symbol) {
            return state + ":" + symbol;
        }
    }
}

