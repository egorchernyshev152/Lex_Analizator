package parser;

import java.util.*;

/**
 * Класс, представляющий контекстно-свободную грамматику.
 *
 * Пример грамматики:
 *
 *   S → begin STATEMENTS end
 *
 *   STATEMENTS → STATEMENT STATEMENTS
 *               | ε
 *
 *   STATEMENT → IDENTIFIER := EXPR ;
 *              | if ( COND ) writeln ( WRITE_ARG ) else writeln ( WRITE_ARG ) ;
 *
 *   EXPR → IDENTIFIER    | INTEGER_LITERAL | FLOAT_LITERAL
 *
 *   COND → REL_EXPR COND_TAIL
 *   COND_TAIL → or REL_EXPR COND_TAIL | ε
 *
 *   REL_EXPR → IDENTIFIER REL_OP IDENTIFIER
 *   REL_OP → < | > | = | <= | >= | <>
 *
 *   WRITE_ARG → STRING_LITERAL
 *
 * Терминалы (точно те строки, которые выдаёт лексер в token.value, за исключением обобщённых «IDENTIFIER» и «INTEGER_LITERAL» и т. д.):
 *   begin, end, if, else, writeln, ":=", ";", "(", ")", "<", ">", "=", "<=", ">=", "<>", "or",
 *   "IDENTIFIER", "INTEGER_LITERAL", "FLOAT_LITERAL", "STRING_LITERAL", "$"
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

    public Set<String> getNonTerminals() {
        return Collections.unmodifiableSet(nonTerminals);
    }

    public Set<String> getTerminals() {
        return Collections.unmodifiableSet(terminals);
    }

    public String getStartSymbol() {
        return startSymbol;
    }

    public Map<String, List<List<String>>> getProductions() {
        return Collections.unmodifiableMap(productions);
    }

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

    public static Grammar exampleGrammar() {
        // 1) Все нетерминалы
        Set<String> nonTerminals = Set.of(
                "S",
                "STATEMENTS",
                "STATEMENT",
                "EXPR",
                "COND",
                "COND_TAIL",
                "REL_EXPR",
                "REL_OP",
                "WRITE_ARG"
        );

        // 2) Все терминалы (ровно те строки, которые лексер положит в token.value,
        //     плюс "IDENTIFIER", "INTEGER_LITERAL", "FLOAT_LITERAL", "STRING_LITERAL", "$")
        Set<String> terminals = Set.of(
                "begin", "end", "if", "else", "writeln",
                ":=", ";", "(", ")",
                "<", ">", "=", "<=", ">=", "<>",
                "or",
                "IDENTIFIER", "INTEGER_LITERAL", "FLOAT_LITERAL", "STRING_LITERAL",
                "$"
        );

        String startSymbol = "S";
        Map<String, List<List<String>>> productions = new HashMap<>();

        // S → begin STATEMENTS end
        productions.put("S", List.of(
                List.of("begin", "STATEMENTS", "end")
        ));

        // STATEMENTS → STATEMENT STATEMENTS | ε
        productions.put("STATEMENTS", List.of(
                List.of("STATEMENT", "STATEMENTS"),
                List.of("ε")
        ));

        // STATEMENT → IDENTIFIER := EXPR ;
        //           | if ( COND ) writeln ( WRITE_ARG ) else writeln ( WRITE_ARG ) ;
        productions.put("STATEMENT", List.of(
                List.of("IDENTIFIER", ":=", "EXPR", ";"),
                List.of("if", "(", "COND", ")", "writeln", "(", "WRITE_ARG", ")", "else", "writeln", "(", "WRITE_ARG", ")", ";")
        ));

        // EXPR → IDENTIFIER | INTEGER_LITERAL | FLOAT_LITERAL
        productions.put("EXPR", List.of(
                List.of("IDENTIFIER"),
                List.of("INTEGER_LITERAL"),
                List.of("FLOAT_LITERAL")
        ));

        // COND → REL_EXPR COND_TAIL
        productions.put("COND", List.of(
                List.of("REL_EXPR", "COND_TAIL")
        ));

        // COND_TAIL → or REL_EXPR COND_TAIL | ε
        productions.put("COND_TAIL", List.of(
                List.of("or", "REL_EXPR", "COND_TAIL"),
                List.of("ε")
        ));

        // REL_EXPR → IDENTIFIER REL_OP IDENTIFIER
        productions.put("REL_EXPR", List.of(
                List.of("IDENTIFIER", "REL_OP", "IDENTIFIER")
        ));

        // REL_OP → < | > | = | <= | >= | <>
        productions.put("REL_OP", List.of(
                List.of("<"),
                List.of(">"),
                List.of("="),
                List.of("<="),
                List.of(">="),
                List.of("<>")
        ));

        // WRITE_ARG → STRING_LITERAL
        productions.put("WRITE_ARG", List.of(
                List.of("STRING_LITERAL")
        ));

        return new Grammar(nonTerminals, terminals, startSymbol, productions);
    }
}
