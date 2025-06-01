package lexer;

import utils.HashTable;

import java.util.*;
import java.util.regex.*;

/**
 * Лексер с генератором токенов на основе регулярных выражений.
 * Отдельно хранит таблицу ключевых слов и таблицу идентификаторов/литералов.
 */
public class Lexer {

    private final String input;
    private int position = 0;
    private int lineNumber = 1;

    private int nextKeywordIndex = 1;
    private int nextIdentifierIndex = 1;

    private final HashTable<String, Integer> keywordTable = new HashTable<>();
    private final HashTable<String, Integer> identifierTable = new HashTable<>();
    private final HashTable<String, Boolean> lexemeTable = new HashTable<>();


    private static class TokenPattern {
        final Token.TokenType type;
        final Pattern pattern;
        final boolean skip;

        TokenPattern(Token.TokenType type, String regex, boolean skip) {
            this.type = type;
            this.pattern = Pattern.compile("^" + regex, Pattern.DOTALL);
            this.skip = skip;
        }
    }

    private final List<TokenPattern> tokenPatterns = Arrays.asList(
            // Ключевые слова — в начале для приоритета
            new TokenPattern(Token.TokenType.KEYWORD_BEGIN, "begin", false),
            new TokenPattern(Token.TokenType.KEYWORD_END, "end", false),
            new TokenPattern(Token.TokenType.KEYWORD_IF, "if", false),
            new TokenPattern(Token.TokenType.KEYWORD_ELSE, "else", false),
            new TokenPattern(Token.TokenType.KEYWORD_FOR, "for", false),
            new TokenPattern(Token.TokenType.KEYWORD_TO, "to", false),
            new TokenPattern(Token.TokenType.KEYWORD_STEP, "step", false),
            new TokenPattern(Token.TokenType.KEYWORD_NEXT, "next", false),
            new TokenPattern(Token.TokenType.KEYWORD_WHILE, "while", false),
            new TokenPattern(Token.TokenType.KEYWORD_READLN, "readln", false),
            new TokenPattern(Token.TokenType.KEYWORD_WRITELN, "writeln", false),
            new TokenPattern(Token.TokenType.KEYWORD_IS, "is", false),
            new TokenPattern(Token.TokenType.KEYWORD_LESS, "less", false),
            new TokenPattern(Token.TokenType.KEYWORD_THAN, "than", false),
            new TokenPattern(Token.TokenType.KEYWORD_GREATER, "greater", false),
            new TokenPattern(Token.TokenType.KEYWORD_OR, "or", false),
            new TokenPattern(Token.TokenType.KEYWORD_EQUAL, "equal", false),

            // Пробельные символы и комментарии (пропускаемые)
            new TokenPattern(Token.TokenType.WHITESPACE, "[ \\t\\r\\f]+", true),
            new TokenPattern(Token.TokenType.NEWLINE, "\\n", true),
            new TokenPattern(Token.TokenType.COMMENT, "\\(\\*(.|\\R)*?\\*\\)", true),

            // Специфичные символы с кавычкой
            new TokenPattern(Token.TokenType.LPAREN_QUOTE, "\\(\"", false),
            new TokenPattern(Token.TokenType.QUOTE_RPAREN, "\"\\)", false),

            // Строковые литералы
            new TokenPattern(Token.TokenType.STRING_LITERAL, "\"(\\\\.|[^\"\\\\])*\"", false),

            // Числа с плавающей точкой и целые
            new TokenPattern(Token.TokenType.FLOAT_LITERAL, "\\d+\\.\\d+([eE][+-]?\\d+)?", false),
            new TokenPattern(Token.TokenType.INTEGER_LITERAL, "\\d+([eE][+-]?\\d+)?", false),

            // Идентификаторы (переменные)
            new TokenPattern(Token.TokenType.IDENTIFIER, "[a-zA-Z][a-zA-Z0-9]*", false),

            // Операторы
            new TokenPattern(Token.TokenType.OP_ASSIGN, ":=", false),
            new TokenPattern(Token.TokenType.OP_RELATION, "<=|>=|<>|<|>|=", false),
            new TokenPattern(Token.TokenType.OP_ADDITIVE, "\\+|-|or", false),
            new TokenPattern(Token.TokenType.OP_MULTIPLICATIVE, "\\*|/|and", false),
            new TokenPattern(Token.TokenType.OP_UNARY, "not|!", false),

            // Разделители
            new TokenPattern(Token.TokenType.SEMICOLON, ";", false),
            new TokenPattern(Token.TokenType.COLON, ":", false),
            new TokenPattern(Token.TokenType.COMMA, ",", false),
            new TokenPattern(Token.TokenType.LPAREN, "\\(", false),
            new TokenPattern(Token.TokenType.RPAREN, "\\)", false)
    );

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            String remaining = input.substring(position);
            boolean matched = false;

            for (TokenPattern tp : tokenPatterns) {
                Matcher matcher = tp.pattern.matcher(remaining);

                if (matcher.find()) {
                    String lexeme = matcher.group();
                    if (lexeme.length() == 0) {
                        throw new RuntimeException("Zero length match at position " + position);
                    }

                    int newlines = countNewlines(lexeme);
                    lineNumber += newlines;
                    position += lexeme.length();

                    if (tp.skip) {
                        matched = true;
                        break;
                    }

                    Token.TokenType type = tp.type;

                    // Записываем в отдельные таблицы ключевые слова и идентификаторы/литералы
                    if ("Keyword".equals(type.getCategory())) {
                        if (!keywordTable.containsKey(lexeme)) {
                            keywordTable.put(lexeme, nextKeywordIndex++);
                        }
                    } else if (type == Token.TokenType.IDENTIFIER
                            || type == Token.TokenType.INTEGER_LITERAL
                            || type == Token.TokenType.FLOAT_LITERAL) {
                        if (!identifierTable.containsKey(lexeme)) {
                            identifierTable.put(lexeme, nextIdentifierIndex++);
                        }
                    }

                    lexemeTable.put(lexeme, true);

                    tokens.add(new Token(type, lexeme, lineNumber));
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException("Unexpected character '" + input.charAt(position) +
                        "' at line " + lineNumber + " pos " + position);
            }
        }

        tokens.add(new Token(Token.TokenType.EOF, "", lineNumber));
        return tokens;
    }

    private int countNewlines(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '\n') count++;
        }
        return count;
    }

    public HashTable<String, Integer> getKeywordTable() {
        return keywordTable;
    }

    public HashTable<String, Integer> getIdentifierTable() {
        return identifierTable;
    }

    public HashTable<String, Boolean> getLexemeTable() {
        return lexemeTable;
    }
}
