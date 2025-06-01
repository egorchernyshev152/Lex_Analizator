package parser;

import java.util.*;

/**
 * LL(1)-парсер, который одновременно строит parse‐tree.
 *
 * Стек содержит узлы ParseTreeNode. Вначале в стек кладутся:
 *   [ParseTreeNode("$", true), ParseTreeNode(startSymbol, false)]
 *
 * Алгоритм:
 *   – Если вершина стека – терминал (или "$"), сравниваем его с текущим токеном:
 *       • если совпадает – pop и advance.
 *       • иначе – ошибка.
 *   – Если вершина стека – нетерминал, берём правило из ParseTable.getRule(A, currentToken):
 *       • если правила нет → ошибка.
 *       • иначе создаём дочерние узлы (ParseTreeNode) в точном порядке RHS.
 *         Дочерние узлы сразу добавляются в children родителя,
 *         а в стек кладутся в обратном порядке (чтобы читать слева направо), **кроме ε**.
 *
 * В конце, если стек пуст и мы прочли все токены, возвращается корень дерева.
 */
public class LL1Parser {
    private final Grammar grammar;
    private final ParseTable parseTable;
    private final String endMarker = "$";

    public LL1Parser(Grammar grammar, ParseTable parseTable) {
        this.grammar = grammar;
        this.parseTable = parseTable;
    }

    /**
     * Разбор входных терминалов с построением parse‐tree.
     * @param tokens список терминалов (String), последний элемент – "$"
     * @return корневой узел parse‐tree
     * @throws RuntimeException при синтаксической ошибке
     */
    public ParseTreeNode parse(List<String> tokens) throws RuntimeException {
        // 1) Создаём корень – нетерминал (startSymbol), и узел "$"
        ParseTreeNode root = new ParseTreeNode(grammar.getStartSymbol(), false);
        ParseTreeNode dollarNode = new ParseTreeNode(endMarker, true);

        // 2) Инициализируем стек: сначала "$", потом корень S
        Stack<ParseTreeNode> stack = new Stack<>();
        stack.push(dollarNode);
        stack.push(root);

        int pos = 0;  // текущая позиция в tokens

        // 3) Пока стек не пуст
        while (!stack.isEmpty()) {
            ParseTreeNode topNode = stack.peek();
            String topSymbol = topNode.name;
            String currentToken = (pos < tokens.size()) ? tokens.get(pos) : null;

            // 3.1) Если вершина – терминал (isTerminal=true) или это "$"
            if (topNode.isTerminal || topSymbol.equals(endMarker)) {
                if (topSymbol.equals(currentToken)) {
                    // совпадение: pop и переход к следующему токену
                    stack.pop();
                    pos++;
                } else {
                    throw new RuntimeException(
                            "Ошибка разбора: ожидался '" + topSymbol + "', найден '" + currentToken + "'");
                }
            } else {
                // 3.2) Вершина – нетерминал
                List<String> rhs = parseTable.getRule(topSymbol, currentToken);
                if (rhs == null) {
                    throw new RuntimeException(
                            "Ошибка разбора: нет правила для (" + topSymbol + ", " + currentToken + ")");
                }

                // pop текущий нетерминал – мы собираемся раскрыть его в rhs
                stack.pop();

                // 3.2.1) Если правило – ε, то добавляем узел "ε" в дерево, но НЕ пушим его в стек
                if (rhs.size() == 1 && rhs.get(0).equals("ε")) {
                    ParseTreeNode epsNode = new ParseTreeNode("ε", true);
                    topNode.addChild(epsNode);
                } else {
                    // 3.2.2) Иначе создаём узлы для каждого символа из RHS
                    List<ParseTreeNode> children = new ArrayList<>();
                    for (String sym : rhs) {
                        boolean isTerm = grammar.getTerminals().contains(sym) || sym.equals("$");
                        ParseTreeNode child = new ParseTreeNode(sym, isTerm);
                        children.add(child);
                    }
                    // Добавляем детей как потомков topNode
                    for (ParseTreeNode child : children) {
                        topNode.addChild(child);
                    }
                    // И пушим их в стек в обратном порядке
                    for (int i = children.size() - 1; i >= 0; i--) {
                        stack.push(children.get(i));
                    }
                }
            }
        }

        // 4) В конце стек пуст. Если позиции не хватило – вход не полностью прочитан
        if (pos != tokens.size()) {
            throw new RuntimeException("Ошибка разбора: вход не полностью прочитан.");
        }

        return root;
    }
}
