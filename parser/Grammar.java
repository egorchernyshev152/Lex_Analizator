package parser;

import java.util.*;

/**
 * Класс, представляющий контекстно-свободную грамматику.
 * Содержит множества нетерминалов и терминалов, стартовый символ,
 * а также правила продукции в виде отображения: нетерминал -> список правил.
 */
public class Grammar {
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final String startSymbol;
    private final Map<String, List<List<String>>> productions;

    public Grammar(Set<String> nonTerminals, Set<String> terminals, String startSymbol,
                   Map<String, List<List<String>>> productions) {
        this.nonTerminals = new HashSet<>(nonTerminals);
        this.terminals = new HashSet<>(terminals);
        this.startSymbol = startSymbol;
        this.productions = new HashMap<>();
        productions.forEach((nt, rules) -> this.productions.put(nt, new ArrayList<>(rules)));
    }

    // Возвращается неизменяемый набор
    public Set<String> getNonTerminals() {
        return Collections.unmodifiableSet(nonTerminals);
    }

    // Возвращается неизменяемый набор
    public Set<String> getTerminals() {
        return Collections.unmodifiableSet(terminals);
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    // Возвращается неизменяемая копия карты правил
    public Map<String, List<List<String>>> getProductions() {
        return Collections.unmodifiableMap(productions);
    }

    // Метод для вывода грамматики в более удобном виде
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Начальный символ: ").append(startSymbol).append("\n");
        sb.append("Нетерминалы: ").append(nonTerminals).append("\n");
        sb.append("Терминалы: ").append(terminals).append("\n");
        sb.append("Продукции:\n");
        for (Map.Entry<String, List<List<String>>> entry : productions.entrySet()) {
            String nt = entry.getKey();
            for (List<String> rhs : entry.getValue()) {
                sb.append("  ").append(nt).append(" → ").append(String.join(" ", rhs)).append("\n");
            }
        }
        return sb.toString();
    }

    // Статический метод для создания Grammar по заданной грамматике с устранённой левой рекурсией
    public static Grammar exampleGrammar() {
        Set<String> nonTerminals = Set.of("S", "STATEMENTS", "STATEMENT", "EXPR", "COND", "WRITE_ARG");
        Set<String> terminals = Set.of(
                "begin", "end", "if", "else", "writeln", ":=", ";", "(", ")", "<",
                "IDENTIFIER", "INTEGER_LITERAL", "FLOAT_LITERAL", "STRING_LITERAL", "$"
        );
        String startSymbol = "S";

        Map<String, List<List<String>>> productions = new HashMap<>();

        productions.put("S", Arrays.asList(
                Arrays.asList("begin", "STATEMENTS", "end")
        ));

        productions.put("STATEMENTS", Arrays.asList(
                Arrays.asList("STATEMENT", "STATEMENTS"),
                Arrays.asList("ε")
        ));

        productions.put("STATEMENT", Arrays.asList(
                Arrays.asList("IDENTIFIER", ":=", "EXPR", ";"),
                Arrays.asList("if", "(", "COND", ")", "writeln", "(", "WRITE_ARG", ")", "else", "writeln", "(", "WRITE_ARG", ")", ";")
        ));

        productions.put("EXPR", Arrays.asList(
                Arrays.asList("INTEGER_LITERAL"),
                Arrays.asList("FLOAT_LITERAL"),
                Arrays.asList("IDENTIFIER")
        ));

        productions.put("COND", Arrays.asList(
                Arrays.asList("IDENTIFIER", "<", "IDENTIFIER")
        ));

        productions.put("WRITE_ARG", Arrays.asList(
                Arrays.asList("STRING_LITERAL")
        ));

        return new Grammar(nonTerminals, terminals, startSymbol, productions);
    }
}
