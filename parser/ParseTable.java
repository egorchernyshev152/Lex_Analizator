package parser;

import java.util.*;

/**
 * Класс ParseTable строит таблицу синтаксического анализа LL(1) для заданной грамматики,
 * используя множества FIRST и FOLLOW.
 */
public class ParseTable {
    // Грамматика, для которой строится таблица
    private final Grammar grammar;
    // Объект для вычисления множеств FIRST и FOLLOW
    private final FirstFollow firstFollow;
    // Сама таблица синтаксического анализа
    private final Map<String, Map<String, List<String>>> parseTable = new HashMap<>();

    // Конструктор создает таблицу синтаксического анализа при инициализации
    public ParseTable(Grammar grammar, FirstFollow firstFollow) {
        this.grammar = grammar;
        this.firstFollow = firstFollow;
        buildParseTable();
    }

    /**
     * Построение таблицы синтаксического анализа LL(1) по алгоритму:
     * Для каждого правила A → α:
     *   - для каждого терминала a из FIRST(α) кроме ε добавить правило в M[A,a]
     *   - если ε ∈ FIRST(α), для каждого b из FOLLOW(A) добавить правило в M[A,b]
     */
    private void buildParseTable() {
        for (String nonTerminal : grammar.getNonTerminals()) {
            // Получаем все правила для данного нетерминала
            List<List<String>> productions = grammar.getProductions().getOrDefault(nonTerminal, Collections.emptyList());
            for (List<String> production : productions) {
                // Вычисляем FIRST множества для последовательности правой части правила
                Set<String> firstSet = computeFirstOfSequence(production);

                // Для каждого терминала из FIRST, кроме ε, добавляем правило в таблицу
                for (String terminal : firstSet) {
                    if (!terminal.equals("ε")) {
                        addToTable(nonTerminal, terminal, production);
                    }
                }

                // Если ε есть в FIRST, добавляем правило для всех терминалов из FOLLOW нетерминала
                if (firstSet.contains("ε")) {
                    Set<String> followSet = firstFollow.getFollow(nonTerminal);
                    for (String terminal : followSet) {
                        addToTable(nonTerminal, terminal, production);
                    }
                }
            }
        }
    }

    /**
     * Добавление правила в таблицу для пары (нетерминал, терминал).
     * Создает новую строку в таблице при необходимости.
     */
    private void addToTable(String nonTerminal, String terminal, List<String> production) {
        parseTable
                .computeIfAbsent(nonTerminal, k -> new HashMap<>())
                .put(terminal, production);
    }

    /**
     * Вычисление множества FIRST для последовательности символов.
     * Учитывает nullable символы (ε).
     */
    private Set<String> computeFirstOfSequence(List<String> sequence) {
        Set<String> result = new HashSet<>();

        // Если правило - это только ε, то FIRST = {ε}
        if (sequence.size() == 1 && sequence.get(0).equals("ε")) {
            result.add("ε");
            return result;
        }

        boolean allNullable = true; // флаг, что все символы в sequence nullable
        for (String sym : sequence) {
            Set<String> firstSym = firstFollow.getFirst(sym);

            // Если FIRST для символа пуст или отсутствует, считаем что символ не nullable
            if (firstSym == null || firstSym.isEmpty()) {
                allNullable = false;
                break;
            }

            // Добавляем все элементы FIRST(sym) в результат
            result.addAll(firstSym);

            // Если FIRST(sym) не содержит ε — дальше не идем
            if (!firstSym.contains("ε")) {
                allNullable = false;
                break;
            } else {
                // Убираем ε, так как учитываем его отдельно
                result.remove("ε");
            }
        }

        // Если все символы nullable — добавляем ε в результат
        if (allNullable) {
            result.add("ε");
        }
        return result;
    }

    // Получаем правило для пары (нетерминал, терминал) из таблицы синтаксического анализа
    public List<String> getRule(String nonTerminal, String terminal) {
        Map<String, List<String>> row = parseTable.get(nonTerminal);
        if (row != null) {
            return row.get(terminal);
        }
        return null;
    }

    // Вывод таблицы синтаксического анализа
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
