package parser;

import java.util.*;

public class LL1Parser {
    private final Grammar grammar;
    private final ParseTable parseTable;
    private final String endMarker = "$";

    public LL1Parser(Grammar grammar, ParseTable parseTable) {
        this.grammar = grammar;
        this.parseTable = parseTable;
    }

    /**
     * Разбор входной последовательности токенов.
     * @param tokens список токенов (терминалов) из лексера, должен заканчиваться endMarker "$"
     * @return true, если строка распознана по грамматике, иначе false
     */
    public boolean parse(List<String> tokens) {
        Stack<String> stack = new Stack<>();
        stack.push(endMarker);
        stack.push(grammar.getStartSymbol());

        int pos = 0;

        while (!stack.isEmpty()) {
            String top = stack.peek();
            String currentToken = (pos < tokens.size()) ? tokens.get(pos) : null;

            // Если вершина стека — терминал или конец, сравниваем с текущим токеном
            if (isTerminal(top) || top.equals(endMarker)) {
                if (top.equals(currentToken)) {
                    stack.pop();
                    pos++;
                } else {
                    System.err.println("Ошибка разбора: ожидался '" + top + "', найден '" + currentToken + "'");
                    return false;
                }
            } else {
                // Вершина — нетерминал, ищем правило в таблице разбора
                List<String> rule = parseTable.getRule(top, currentToken);
                if (rule == null) {
                    System.err.println("Ошибка разбора: нет правила для (" + top + ", " + currentToken + ")");
                    return false;
                }
                stack.pop();
                // Если правило не пустое (не ε), кладём правую часть в стек в обратном порядке
                if (!(rule.size() == 1 && rule.get(0).equals("ε"))) {
                    for (int i = rule.size() - 1; i >= 0; i--) {
                        stack.push(rule.get(i));
                    }
                }
            }
        }

        if (pos == tokens.size()) {
            System.out.println("Строка успешно распознана.");
            return true;
        } else {
            System.err.println("Ошибка разбора: вход не полностью прочитан.");
            return false;
        }
    }

    private boolean isTerminal(String symbol) {
        return grammar.getTerminals().contains(symbol);
    }
}
