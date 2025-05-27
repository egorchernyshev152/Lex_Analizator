package parser;

import java.util.*;

/**
 * Класс для вычисления множеств FIRST и FOLLOW для грамматики.
 * Используется для построения таблицы разбора LL(1).
 */
public class FirstFollow {
    private final Grammar grammar;
    private final Map<String, Set<String>> firstSets = new HashMap<>();
    private final Map<String, Set<String>> followSets = new HashMap<>();

    public FirstFollow(Grammar grammar) {
        this.grammar = grammar;
        initialize();        // Инициализация множеств FIRST и FOLLOW
        computeFirstSets();  // Вычисление FIRST
        computeFollowSets(); // Вычисление FOLLOW
    }

    // Инициализация FIRST и FOLLOW
    // Терминалы имеют FIRST равный себе
    // Для стартового символа FOLLOW содержит маркер конца '$'
    private void initialize() {
        for (String t : grammar.getTerminals()) {
            firstSets.put(t, new HashSet<>(Collections.singleton(t)));
        }
        for (String nt : grammar.getNonTerminals()) {
            firstSets.put(nt, new HashSet<>());
            followSets.put(nt, new HashSet<>());
        }
        followSets.get(grammar.getStartSymbol()).add("$");
    }

    // Итеративное вычисление FIRST множеств для всех нетерминалов
    public void computeFirstSets() {
        boolean changed;
        do {
            changed = false;
            for (String nt : grammar.getNonTerminals()) {
                List<List<String>> productions = grammar.getProductions().getOrDefault(nt, Collections.emptyList());
                for (List<String> production : productions) {
                    int before = firstSets.get(nt).size();
                    addFirstOfSequence(nt, production);
                    int after = firstSets.get(nt).size();
                    if (after > before) changed = true;
                }
            }
        } while (changed);
    }

    // Добавление в FIRST(nt) множество терминалов, которые начинаются у production
    private void addFirstOfSequence(String nt, List<String> sequence) {
        Set<String> firstNt = firstSets.get(nt);
        boolean allNullable = true;

        for (String sym : sequence) {
            Set<String> firstSym = firstSets.get(sym);
            if (firstSym == null) { // терминал
                firstNt.add(sym);
                allNullable = false;
                break;
            }
            firstNt.addAll(firstSym);
            if (!firstSym.contains("ε")) {
                allNullable = false;
                break;
            } else {
                firstNt.remove("ε"); // ε не входит в FIRST кроме как для nullable символов
            }
        }

        if (allNullable) {
            firstNt.add("ε");
        }
    }

    // Итеративное вычисление FOLLOW множеств для всех нетерминалов
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
                        int before = followSym.size();

                        // FIRST множества для цепочки справа от sym
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
                            }
                        }

                        // Добавить в FOLLOW(sym) FIRST(β)
                        followSym.addAll(firstBeta);
                        // Если β nullable, добавить FOLLOW(нетерминала слева)
                        if (betaNullable) {
                            followSym.addAll(followSets.get(nt));
                        }

                        int after = followSym.size();
                        if (after > before) changed = true;
                    }
                }
            }
        } while (changed);
    }

    // Геттеры и вывод FIRST и FOLLOW множеств
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
