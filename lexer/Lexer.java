package lexer;

import utils.HashTable;

import java.util.*;
import java.util.regex.*;

/**
 * Лексер с генератором токенов на основе регулярных выражений.
 * Хранит таблицу ключевых слов и таблицу идентификаторов/литералов.
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

    // … внутри класса Lexer …

    private final List<TokenPattern> tokenPatterns = Arrays.asList(
            // 1) Ключевые слова — в начале для приоритета
            new TokenPattern(Token.TokenType.KEYWORD_BEGIN,   "begin",    false),
            new TokenPattern(Token.TokenType.KEYWORD_END,     "end",      false),
            new TokenPattern(Token.TokenType.KEYWORD_IF,      "if",       false),
            new TokenPattern(Token.TokenType.KEYWORD_ELSE,    "else",     false),
            new TokenPattern(Token.TokenType.KEYWORD_FOR,     "for",      false),
            new TokenPattern(Token.TokenType.KEYWORD_TO,      "to",       false),
            new TokenPattern(Token.TokenType.KEYWORD_STEP,    "step",     false),
            new TokenPattern(Token.TokenType.KEYWORD_NEXT,    "next",     false),
            new TokenPattern(Token.TokenType.KEYWORD_WHILE,   "while",    false),
            new TokenPattern(Token.TokenType.KEYWORD_READLN,  "readln",   false),
            new TokenPattern(Token.TokenType.KEYWORD_WRITELN, "writeln",  false),
            new TokenPattern(Token.TokenType.KEYWORD_IS,      "is",       false),
            new TokenPattern(Token.TokenType.KEYWORD_LESS,    "less",     false),
            new TokenPattern(Token.TokenType.KEYWORD_THAN,    "than",     false),
            new TokenPattern(Token.TokenType.KEYWORD_GREATER, "greater",  false),
            new TokenPattern(Token.TokenType.KEYWORD_OR,      "or",       false),
            new TokenPattern(Token.TokenType.KEYWORD_EQUAL,   "equal",    false),

            // 2) Пропускаемые: пробелы, табы, возвращение каретки, перевод строки, комментарии
            new TokenPattern(Token.TokenType.WHITESPACE, "[ \\t\\r\\f]+",    true),
            new TokenPattern(Token.TokenType.NEWLINE,    "\\n",               true),
            new TokenPattern(Token.TokenType.COMMENT,    "\\(\\*(.|\\R)*?\\*\\)", true),

            // 3) Строковый литерал
            new TokenPattern(Token.TokenType.STRING_LITERAL, "\"(\\\\.|[^\"\\\\])*\"", false),

            // 4) Числа
            new TokenPattern(Token.TokenType.FLOAT_LITERAL,   "\\d+\\.\\d+([eE][+-]?\\d+)?", false),
            new TokenPattern(Token.TokenType.INTEGER_LITERAL, "\\d+([eE][+-]?\\d+)?",        false),

            // 5) Идентификаторы
            new TokenPattern(Token.TokenType.IDENTIFIER, "[a-zA-Z][a-zA-Z0-9]*", false),

            // 6) Оператор присваивания ":="
            new TokenPattern(Token.TokenType.OP_ASSIGN, ":=", false),

            // 7) Разделители (очень важно — до OP_RELATION!)
            new TokenPattern(Token.TokenType.SEMICOLON, ";",   false),
            new TokenPattern(Token.TokenType.COLON,     ":",   false),
            new TokenPattern(Token.TokenType.COMMA,     ",",   false),
            new TokenPattern(Token.TokenType.LPAREN,    "\\(", false),
            new TokenPattern(Token.TokenType.RPAREN,    "\\)", false),

            // 8) Реляционные операторы: <=, >=, <>, <, >, =
            //    Теперь группируем весь «список альтернатив» в круглые скобки,
            //    чтобы '^' (добавляемый автоматически при компиляции) применялся ко всем сразу.
            new TokenPattern(Token.TokenType.OP_RELATION, "(?:<=|>=|<>|<|>|=)", false),

            // 9) Арифметические/логические операторы (любой из этих)
            new TokenPattern(Token.TokenType.OP_ADDITIVE,       "\\+|-",      false),
            new TokenPattern(Token.TokenType.OP_MULTIPLICATIVE, "\\*|/|and", false),
            new TokenPattern(Token.TokenType.OP_UNARY,          "not|!",      false)
    );


    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Разбивает входную строку на список токенов,
     * одновременно вычисляя для каждого токена номер строки, столбец и порядковый индекс.
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int tokenIndex = 0;
        int lastLineOffset = 0; // абсолютная позиция в input, с которой началась текущая строка

        while (position < input.length()) {
            String remaining = input.substring(position);
            boolean matched = false;

            for (TokenPattern tp : tokenPatterns) {
                Matcher matcher = tp.pattern.matcher(remaining);
                if (matcher.find()) {
                    String lexeme = matcher.group();
                    if (lexeme.isEmpty()) {
                        throw new RuntimeException("Zero length match at position " + position);
                    }

                    // Подсчёт переносов строки внутри lexeme, чтобы обновить lineNumber и lastLineOffset
                    int newlines = countNewlines(lexeme);
                    if (newlines > 0) {
                        int lastNewlineInLexeme = lexeme.lastIndexOf('\n');
                        lastLineOffset = position + lastNewlineInLexeme + 1;
                    }
                    lineNumber += newlines;

                    // Вычисляем столбец (column): позиция символа в текущей строке (начиная с 1)
                    int column = position - lastLineOffset + 1;

                    position += lexeme.length();

                    if (tp.skip) {
                        // Пропускаем пробелы/комментарии/новые строки
                        matched = true;
                        break;
                    }

                    Token.TokenType type = tp.type;

                    // Добавляем в keywordTable или identifierTable при необходимости
                    if ("Keyword".equals(type.getCategory())) {
                        if (!keywordTable.containsKey(lexeme)) {
                            keywordTable.put(lexeme, nextKeywordIndex++);
                        }
                    } else if (type == Token.TokenType.IDENTIFIER
                            || type == Token.TokenType.INTEGER_LITERAL
                            || type == Token.TokenType.FLOAT_LITERAL
                            || type == Token.TokenType.STRING_LITERAL) {
                        if (!identifierTable.containsKey(lexeme)) {
                            identifierTable.put(lexeme, nextIdentifierIndex++);
                        }
                    }

                    lexemeTable.put(lexeme, true);

                    // Создаём токен с дополнительными полями: lineNumber, column и tokenIndex
                    Token token = new Token(type, lexeme, lineNumber, column, tokenIndex);
                    tokens.add(token);
                    tokenIndex++;

                    matched = true;
                    break;
                }
            }

            if (!matched) {
                char unexpected = input.charAt(position);
                throw new RuntimeException("Unexpected character '" + unexpected +
                        "' at line " + lineNumber + " pos " + position);
            }
        }

        // Добавляем EOF-токен с индексом tokenIndex, column = 1 (на следующей строке)
        Token eofToken = new Token(Token.TokenType.EOF, "", lineNumber, 1, tokenIndex);
        tokens.add(eofToken);

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
