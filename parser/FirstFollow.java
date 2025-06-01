package parser;

import java.util.*;

/**
 * Класс для вычисления множеств FIRST и FOLLOW для грамматики.
 * Используется при построении таблицы LL(1).
 */
public class FirstFollow {
    private final Grammar grammar;
    private final Map<String, Set<String>> firstSets = new HashMap<>();
    private final Map<String, Set<String>> followSets = new HashMap<>();

    public FirstFollow(Grammar grammar) {
        this.grammar = grammar;
        initialize();
        computeFirstSets();
        computeFollowSets();
    }

    private void initialize() {
        // Терминалы: FIRST(т) = { t }
        for (String t : grammar.getTerminals()) {
            firstSets.put(t, new HashSet<>(Collections.singleton(t)));
        }
        // Нетерминалы: FIRST пусто, FOLLOW пусто
        for (String nt : grammar.getNonTerminals()) {
            firstSets.put(nt, new HashSet<>());
            followSets.put(nt, new HashSet<>());
        }
        // Для стартового символа FOLLOW( start ) содержит $
        followSets.get(grammar.getStartSymbol()).add("$");
    }

    public void computeFirstSets() {
        boolean changed;
        do {
            changed = false;
            for (String nt : grammar.getNonTerminals()) {
                List<List<String>> productions = grammar.getProductions().getOrDefault(nt, Collections.emptyList());
                for (List<String> production : productions) {
                    int beforeSize = firstSets.get(nt).size();
                    addFirstOfSequence(nt, production);
                    int afterSize = firstSets.get(nt).size();
                    if (afterSize > beforeSize) changed = true;
                }
            }
        } while (changed);
    }

    private void addFirstOfSequence(String nt, List<String> sequence) {
        Set<String> firstNt = firstSets.get(nt);
        boolean allNullable = true;

        for (String sym : sequence) {
            Set<String> firstSym = firstSets.get(sym);
            if (firstSym == null) {
                // sym — терминал
                firstNt.add(sym);
                allNullable = false;
                break;
            }
            firstNt.addAll(firstSym);
            if (!firstSym.contains("ε")) {
                allNullable = false;
                break;
            } else {
                firstNt.remove("ε");
            }
        }
        if (allNullable) {
            firstNt.add("ε");
        }
    }

    public void computeFollowSets() {
        boolean changed;
        do {
            changed = false;
            for (String nt : grammar.getNonTerminals()) {
                List<List<String>> productions = grammar.getProductions().getOrDefault(nt, Collections.emptyList());
                for (List<String> production : productions) {
                    for (int i = 0; i < production.size(); i++) {
                        String sym = production.get(i);
                        if (!grammar.getNonTerminals().contains(sym)) continue;

                        Set<String> followSym = followSets.get(sym);
                        int beforeSize = followSym.size();

                        // FIRST( β ), β = sequence справа от sym
                        Set<String> firstBeta = new HashSet<>();
                        boolean betaNullable = true;

                        for (int j = i + 1; j < production.size(); j++) {
                            String nextSym = production.get(j);
                            Set<String> firstNext = firstSets.get(nextSym);
                            for (String s : firstNext) {
                                if (!s.equals("ε")) {
                                    firstBeta.add(s);
                                }
                            }
                            if (!firstNext.contains("ε")) {
                                betaNullable = false;
                                break;
                            } else {
                                firstBeta.remove("ε");
                            }
                        }

                        followSym.addAll(firstBeta);
                        if (betaNullable) {
                            followSym.addAll(followSets.get(nt));
                        }

                        int afterSize = followSym.size();
                        if (afterSize > beforeSize) changed = true;
                    }
                }
            }
        } while (changed);
    }

    public Set<String> getFirst(String symbol) {
        return firstSets.getOrDefault(symbol, Collections.emptySet());
    }

    public Set<String> getFollow(String nonTerminal) {
        return followSets.getOrDefault(nonTerminal, Collections.emptySet());
    }

    public void printFirstFollow() {
        System.out.println("\nМножества FIRST:");
        for (String nt : grammar.getNonTerminals()) {
            System.out.println(nt + " : " + firstSets.get(nt));
        }
        System.out.println("\nМножества FOLLOW:");
        for (String nt : grammar.getNonTerminals()) {
            System.out.println(nt + " : " + followSets.get(nt));
        }
    }
}
