import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.TokenType;

import static utils.HashTable.printSortedTable;

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

        System.out.println("=== Таблица ключевых слов (имя → индекс) ===");
        printSortedTable(lexer.getKeywordTable());

        System.out.println("\n=== Таблица идентификаторов (имя → индекс) ===");
        printSortedTable(lexer.getIdentifierTable());

        List<String> tokenValues = getTokenValuesForParser(tokens);
        System.out.println("\n=== Token Values (для LL(1)) ===");
        System.out.println(tokenValues);
    }

    private static List<String> getTokenValuesForParser(List<Token> tokens) {
        List<String> tokenValues = new java.util.ArrayList<>();
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
