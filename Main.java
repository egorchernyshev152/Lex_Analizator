import lexer.Lexer;
import lexer.Token;
import parser.Grammar;
import parser.FirstFollow;
import parser.ParseTable;
import parser.LL1Parser;
import utils.HashTable;
import utils.BinarySearchTree;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Ошибка: Укажите путь к файлу с исходным кодом.");
            return;
        }

        String filePath = args[0];
        String testProgram;

        try {
            testProgram = Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return;
        }

        Lexer lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        Lexer.printTokensTable(tokens);

        // Хэш-таблица токенов
        HashTable<String, String> table = new HashTable<>();
        for (Token token : tokens) {
            if (token.type == Token.TokenType.IDENTIFIER || token.type.name().startsWith("KEYWORD")) {
                table.put(token.value, token.type.toString());
            }
        }
        System.out.println("\nХэш-таблица токенов:");
        table.printTable();

        // Бинарное дерево поиска
        BinarySearchTree<String> bst = new BinarySearchTree<>();
        for (Token token : tokens) {
            if (token.type == Token.TokenType.IDENTIFIER || token.type.name().startsWith("KEYWORD")) {
                int groupNumber = token.type.name().startsWith("KEYWORD") ? 1 : 2;
                bst.insert(token.value, groupNumber);
            }
        }
        System.out.println("\nБинарное дерево поиска (идентификаторы и ключевые слова):");
        bst.inOrderTraversal();
        bst.printTree();

        // Парсинг

        // Преобразуем токены в список терминальных символов
        List<String> inputTerminals = new ArrayList<>();
        for (Token token : tokens) {
            if (token.type == Token.TokenType.COMMENT || token.type == Token.TokenType.NEWLINE) {
                continue;
            }
            switch (token.type) {
                case IDENTIFIER:
                    inputTerminals.add("IDENTIFIER");
                    break;
                case INTEGER_LITERAL:
                    inputTerminals.add("INTEGER_LITERAL");
                    break;
                case FLOAT_LITERAL:
                    inputTerminals.add("FLOAT_LITERAL");
                    break;
                case STRING_LITERAL:
                    inputTerminals.add("STRING_LITERAL");
                    break;
                default:
                    // Для ключевых слов, операторов, разделителей — проверяем, что value не пустое
                    if (!token.value.isEmpty()) {
                        inputTerminals.add(token.value);
                    }
            }
        }
        inputTerminals.add("$"); // маркер конца


        // Загружаем грамматику
        Grammar grammar = Grammar.exampleGrammar();

        // Вычисляем FIRST и FOLLOW
        FirstFollow ff = new FirstFollow(grammar);
        ff.printFirstFollow();

        // Строим таблицу синтаксического анализа
        ParseTable parseTable = new ParseTable(grammar, ff);
        parseTable.printParseTable();

        // Создаём парсер и запускаем разбор
        LL1Parser parser = new LL1Parser(grammar, parseTable);
        boolean result = parser.parse(inputTerminals);

        System.out.println("\nРезультат разбора: " + (result ? "Успешен" : "Ошибка"));
    }
}
