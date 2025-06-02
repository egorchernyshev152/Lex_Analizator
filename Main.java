import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.TokenType;
import static utils.HashTable.printSortedTable;

import parser.Grammar;
import parser.FirstFollow;
import parser.ParseTable;
import parser.LL1Parser;
import parser.ParseTreeNode;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Использование: java Main <sourcefile>");
            return;
        }

        String source = new String(Files.readAllBytes(Paths.get(args[0])));
        Lexer lexer = new Lexer(source);
        List<Token> tokens;
        try {
            tokens = lexer.tokenize();
        } catch (RuntimeException e) {
            System.err.println("Ошибка лексического анализа: " + e.getMessage());
            return;
        }

        // ========== ПОДГОТОВКА: таблица для любых токенов, кроме IDENTIFIER и KEYWORD ==========
        Map<String, Integer> otherTokenTable = new LinkedHashMap<>();
        int nextOtherTokenIndex = 1;
        // ======================================================================================

        // === 1) ДЕТАЛЬНЫЙ СПИСОК ТОКЕНОВ ===
        System.out.println("=== ДЕТАЛЬНЫЙ СПИСОК ТОКЕНОВ (type → value [line,index] → num/hash) ===");
        for (Token t : tokens) {
            if (t.type != TokenType.WHITESPACE
                    && t.type != TokenType.NEWLINE
                    && t.type != TokenType.COMMENT) {
                String quoted = "'" + t.value + "'";
                String extraValue;

                // 1) Если это IDENTIFIER → выводим hashCode() лексемы
                if (t.type == TokenType.IDENTIFIER) {
                    extraValue = String.valueOf(t.value.hashCode());
                }
                // 2) Если это KEYWORD → получаем номер из keywordTable
                else if ("Keyword".equals(t.type.getCategory())) {
                    Integer kwNum = lexer.getKeywordTable().get(t.value);
                    extraValue = (kwNum != null ? kwNum.toString() : "");
                }
                // 3) Во всех остальных случаях (числа, разделители, операторы, литералы) — присваиваем собственный индекс
                else {
                    if (!otherTokenTable.containsKey(t.value)) {
                        otherTokenTable.put(t.value, nextOtherTokenIndex++);
                    }
                    extraValue = otherTokenTable.get(t.value).toString();
                }

                System.out.printf(
                        "  %-15s → %-30s  [строка: %3d, токен: %3d]  %-10s%n",
                        t.type, quoted, t.lineNumber, t.index, extraValue
                );
            }
        }

        // === 2) Таблицы ключевых слов и идентификаторов ===
        System.out.println("\n=== Таблица ключевых слов (имя → индекс) ===");
        printSortedTable(lexer.getKeywordTable());

        System.out.println("\n=== Таблица идентификаторов (имя → индекс) ===");
        printSortedTable(lexer.getIdentifierTable());

        // === 3) Преобразуем List<Token> → List<String> для LL(1)-парсера ===
        List<String> tokenValues = getTokenValuesForParser(tokens);
        System.out.println("\n>>> tokenValues = " + tokenValues);

        // === 4) Построение LL(1)-таблицы и дерева разбора ===
        Grammar grammar = Grammar.exampleGrammar();
        FirstFollow ff = new FirstFollow(grammar);
        ParseTable pt = new ParseTable(grammar, ff);
        pt.printParseTable();

        LL1Parser parser = new LL1Parser(grammar, pt);
        try {
            ParseTreeNode tree = parser.parse(tokenValues);
            System.out.println("\n=== Дерево разбора ===");
            tree.print("");
            System.out.println("Успех!");
        } catch (RuntimeException ex) {
            System.err.println("\nОшибка синтаксического анализа: " + ex.getMessage());
        }
    }

    /**
     * Преобразует List<Token> в List<String> для LL(1)-парсера.
     * Отбрасываем WHITESPACE, NEWLINE, COMMENT.
     * Для IDENTIFIER, INTEGER_LITERAL, FLOAT_LITERAL, STRING_LITERAL –
     *    кладём обобщённые метки ("IDENTIFIER", "INTEGER_LITERAL" и т. д.).
     * Для всех остальных – кладём token.value напрямую (":=", "<", "=", ")", ";", "begin", "if", ...).
     */
    private static List<String> getTokenValuesForParser(List<Token> tokens) {
        List<String> tokenValues = new ArrayList<>();
        for (Token token : tokens) {
            if (token.type == TokenType.WHITESPACE
                    || token.type == TokenType.NEWLINE
                    || token.type == TokenType.COMMENT) {
                continue;
            }
            switch (token.type) {
                case IDENTIFIER:
                    tokenValues.add("IDENTIFIER");
                    break;
                case INTEGER_LITERAL:
                    tokenValues.add("INTEGER_LITERAL");
                    break;
                case FLOAT_LITERAL:
                    tokenValues.add("FLOAT_LITERAL");
                    break;
                case STRING_LITERAL:
                    tokenValues.add("STRING_LITERAL");
                    break;
                default:
                    if (!token.value.isEmpty()) {
                        tokenValues.add(token.value);
                    }
                    break;
            }
        }
        tokenValues.add("$");
        return tokenValues;
    }
}
