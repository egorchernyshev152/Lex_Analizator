package parser;

import java.util.*;

/**
 * Класс ParseTable строит LL(1)-таблицу для заданной грамматики.
 */
public class ParseTable {
    private final Grammar grammar;
    private final FirstFollow firstFollow;
    // parseTable: нетерминал → ( терминал → RHS_правило )
    private final Map<String, Map<String, List<String>>> parseTable = new HashMap<>();

    public ParseTable(Grammar grammar, FirstFollow firstFollow) {
        this.grammar = grammar;
        this.firstFollow = firstFollow;
        buildParseTable();
    }

    private void buildParseTable() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<List<String>> productions = grammar.getProductions().getOrDefault(nonTerminal, Collections.emptyList());
            for (List<String> production : productions) {
                Set<String> firstSet = computeFirstOfSequence(production);

                for (String terminal : firstSet) {
                    if (!terminal.equals("ε")) {
                        addToTable(nonTerminal, terminal, production);
                    }
                }
                if (firstSet.contains("ε")) {
                    Set<String> followSet = firstFollow.getFollow(nonTerminal);
                    for (String terminal : followSet) {
                        addToTable(nonTerminal, terminal, production);
                    }
                }
            }
        }
    }

    private void addToTable(String nonTerminal, String terminal, List<String> production) {
        parseTable
                .computeIfAbsent(nonTerminal, k -> new HashMap<>())
                .put(terminal, production);
    }

    private Set<String> computeFirstOfSequence(List<String> sequence) {
        Set<String> result = new HashSet<>();

        if (sequence.size() == 1 && sequence.get(0).equals("ε")) {
            result.add("ε");
            return result;
        }

        boolean allNullable = true;
        for (String sym : sequence) {
            Set<String> firstSym = firstFollow.getFirst(sym);
            if (firstSym == null || firstSym.isEmpty()) {
                // терминал
                result.add(sym);
                allNullable = false;
                break;
            }
            result.addAll(firstSym);
            if (!firstSym.contains("ε")) {
                allNullable = false;
                break;
            } else {
                result.remove("ε");
            }
        }
        if (allNullable) {
            result.add("ε");
        }
        return result;
    }

    public List<String> getRule(String nonTerminal, String terminal) {
        Map<String, List<String>> row = parseTable.get(nonTerminal);
        if (row != null) {
            return row.get(terminal);
        }
        return null;
    }

    public void printParseTable() {
        System.out.println("\nТаблица синтаксического анализа:");
        for (String nt : parseTable.keySet()) {
            Map<String, List<String>> row = parseTable.get(nt);
            for (String t : row.keySet()) {
                System.out.println("M[" + nt + ", " + t + "] = " + String.join(" ", row.get(t)));
            }
        }
    }
}
