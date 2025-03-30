import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position;
    private int lineNumber;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.lineNumber = 1;
    }

    // Типы токенов
    public enum TokenType {
        // Ключевые слова
        KEYWORD_BEGIN, KEYWORD_END, KEYWORD_IF, KEYWORD_ELSE, KEYWORD_FOR,
        KEYWORD_TO, KEYWORD_STEP, KEYWORD_NEXT, KEYWORD_WHILE,
        KEYWORD_READLN, KEYWORD_WRITELN,

        // Типы данных
        TYPE_INT,      // %
        TYPE_FLOAT,    // !
        TYPE_STRING,   // $

        // Операторы
        OP_RELATION,   // !=, ==, <, <=, >, >=
        OP_ADDITIVE,   // +, -, ||
        OP_MULTIPLICATIVE, // *, /, &&
        OP_UNARY,      // !
        OP_ASSIGN,     // :=

        // Разделители
        SEMICOLON,     // ;
        COLON,         // :
        COMMA,         // ,
        LPAREN,        // (
        RPAREN,        // )

        // Идентификаторы и литералы
        IDENTIFIER,
        INTEGER_LITERAL,
        FLOAT_LITERAL,
        STRING_LITERAL,

        // Комментарии и прочее
        COMMENT,
        NEWLINE,
        EOF
    }

    // Класс для представления токена
    public static class Token {
        public final TokenType type;
        public final String value;
        public final int lineNumber;

        public Token(TokenType type, String value, int lineNumber) {
            this.type = type;
            this.value = value;
            this.lineNumber = lineNumber;
        }

        @Override
        public String toString() {
            return String.format("Token(%s, '%s', line %d)", type, value, lineNumber);
        }
    }

    // Проверка на конец файла
    private boolean isEOF() {
        return position >= input.length();
    }

    // Получение текущего символа
    private char peek() {
        if (isEOF()) return '\0';
        return input.charAt(position);
    }

    // Получение следующего символа
    private char next() {
        if (isEOF())
            return '\0';
        char c = input.charAt(position++);
        if (c == '\n')
            lineNumber++;
        return c;
    }

    // Пропуск пробельных символов
    private void skipWhitespace() {
        while (!isEOF() && Character.isWhitespace(peek()) && peek() != '\n') {
            next();
        }
    }

    // Проверка, является ли символ началом идентификатора
    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    // Проверка, является ли символ частью идентификатора
    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // Проверка на цифру
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Чтение идентификатора или ключевого слова
    private Token readIdentifierOrKeyword() {
        int start = position;
        while (!isEOF() && isIdentifierPart(peek())) {
            next();
        }
        String value = input.substring(start, position);

        // Проверка ключевых слов
        switch (value.toLowerCase()) {
            case "begin": return new Token(TokenType.KEYWORD_BEGIN, value, lineNumber);
            case "end": return new Token(TokenType.KEYWORD_END, value, lineNumber);
            case "if": return new Token(TokenType.KEYWORD_IF, value, lineNumber);
            case "else": return new Token(TokenType.KEYWORD_ELSE, value, lineNumber);
            case "for": return new Token(TokenType.KEYWORD_FOR, value, lineNumber);
            case "to": return new Token(TokenType.KEYWORD_TO, value, lineNumber);
            case "step": return new Token(TokenType.KEYWORD_STEP, value, lineNumber);
            case "next": return new Token(TokenType.KEYWORD_NEXT, value, lineNumber);
            case "while": return new Token(TokenType.KEYWORD_WHILE, value, lineNumber);
            case "readln": return new Token(TokenType.KEYWORD_READLN, value, lineNumber);
            case "writeln": return new Token(TokenType.KEYWORD_WRITELN, value, lineNumber);
            default: return new Token(TokenType.IDENTIFIER, value, lineNumber);
        }
    }

    // Чтение числа (целого или с плавающей точкой)
    private Token readNumber() {
        int start = position;
        boolean isFloat = false;

        // Читаем целую часть
        while (!isEOF() && isDigit(peek())) {
            next();
        }

        // Проверяем на наличие точки
        if (!isEOF() && peek() == '.') {
            isFloat = true;
            next();

            // Читаем дробную часть
            while (!isEOF() && isDigit(peek())) {
                next();
            }
        }

        String value = input.substring(start, position);
        return isFloat
                ? new Token(TokenType.FLOAT_LITERAL, value, lineNumber)
                : new Token(TokenType.INTEGER_LITERAL, value, lineNumber);
    }

    // Чтение строкового литерала
    private Token readString() {
        StringBuilder sb = new StringBuilder();
        next(); // Пропускаем открывающую кавычку

        while (!isEOF() && peek() != '"') {
            if (peek() == '\\') {
                next(); // Пропускаем обратный слеш
                // Обработка escape-последовательностей
                switch (peek()) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append('\\').append(peek()); break;
                }
                next();
            } else {
                sb.append(next());
            }
        }

        if (isEOF()) {
            throw new RuntimeException("Unterminated string literal at line " + lineNumber);
        }

        next(); // Пропускаем закрывающую кавычку
        return new Token(TokenType.STRING_LITERAL, sb.toString(), lineNumber);
    }

    // Чтение комментария
    private Token readComment() {
        int startLine = lineNumber;
        next(); // Пропускаем '('

        if (isEOF() || peek() != '*') {
            return new Token(TokenType.LPAREN, "(", startLine);
        }

        next(); // Пропускаем '*'
        StringBuilder sb = new StringBuilder();

        while (!isEOF()) {
            if (peek() == '*' && position + 1 < input.length() && input.charAt(position + 1) == ')') {
                next(); // Пропускаем '*'
                next(); // Пропускаем ')'
                return new Token(TokenType.COMMENT, sb.toString(), startLine);
            }
            sb.append(next());
        }

        throw new RuntimeException("Unterminated comment starting at line " + startLine);
    }

    // Чтение оператора отношения
    private Token readRelationOperator() {
        int start = position;
        char firstChar = next();

        switch (firstChar) {
            case '=':
                if (peek() == '=') {
                    next();
                    return new Token(TokenType.OP_RELATION, "==", lineNumber);
                }
                return new Token(TokenType.OP_RELATION, "=", lineNumber);
            case '!':
                if (peek() == '=') {
                    next();
                    return new Token(TokenType.OP_RELATION, "!=", lineNumber);
                }
                return new Token(TokenType.OP_UNARY, "!", lineNumber);
            case '<':
                if (peek() == '=') {
                    next();
                    return new Token(TokenType.OP_RELATION, "<=", lineNumber);
                }
                return new Token(TokenType.OP_RELATION, "<", lineNumber);
            case '>':
                if (peek() == '=') {
                    next();
                    return new Token(TokenType.OP_RELATION, ">=", lineNumber);
                }
                return new Token(TokenType.OP_RELATION, ">", lineNumber);
            default:
                throw new RuntimeException("Unexpected character: " + firstChar);
        }
    }

    // Чтение оператора присваивания
    private Token readAssignmentOperator() {
        char firstChar = next();
        if (firstChar == ':' && peek() == '=') {
            next();
            return new Token(TokenType.OP_ASSIGN, ":=", lineNumber);
        }
        throw new RuntimeException("Unexpected character: " + firstChar);
    }

    // Основной метод лексического анализа
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isEOF()) {
            char current = peek();

            // Пропускаем пробелы
            if (Character.isWhitespace(current)) {
                if (current == '\n') {
                    tokens.add(new Token(TokenType.NEWLINE, "\\n", lineNumber));
                    next();
                } else {
                    skipWhitespace();
                }
                continue;
            }

            // Комментарии
            if (current == '(' && position + 1 < input.length() && input.charAt(position + 1) == '*') {
                tokens.add(readComment());
                continue;
            }

            // Идентификаторы и ключевые слова
            if (isIdentifierStart(current)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            // Числа
            if (isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            // Строковые литералы
            if (current == '"') {
                tokens.add(readString());
                continue;
            }

            // Операторы и разделители
            switch (current) {
                case ';':
                    next();
                    tokens.add(new Token(TokenType.SEMICOLON, ";", lineNumber));
                    break;
                case ':':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                        tokens.add(readAssignmentOperator());
                    } else {
                        next();
                        tokens.add(new Token(TokenType.COLON, ":", lineNumber));
                    }
                    break;
                case ',':
                    next();
                    tokens.add(new Token(TokenType.COMMA, ",", lineNumber));
                    break;
                case '(':
                    next();
                    tokens.add(new Token(TokenType.LPAREN, "(", lineNumber));
                    break;
                case ')':
                    next();
                    tokens.add(new Token(TokenType.RPAREN, ")", lineNumber));
                    break;
                case '+':
                case '-':
                    next();
                    tokens.add(new Token(TokenType.OP_ADDITIVE, String.valueOf(current), lineNumber));
                    break;
                case '|':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '|') {
                        next();
                        next();
                        tokens.add(new Token(TokenType.OP_ADDITIVE, "||", lineNumber));
                    } else {
                        throw new RuntimeException("Unexpected character: " + current);
                    }
                    break;
                case '*':
                case '/':
                    next();
                    tokens.add(new Token(TokenType.OP_MULTIPLICATIVE, String.valueOf(current), lineNumber));
                    break;
                case '&':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '&') {
                        next();
                        next();
                        tokens.add(new Token(TokenType.OP_MULTIPLICATIVE, "&&", lineNumber));
                    } else {
                        throw new RuntimeException("Unexpected character: " + current);
                    }
                    break;
                case '%':
                    next();
                    tokens.add(new Token(TokenType.TYPE_INT, "%", lineNumber));
                    break;
                case '!':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                        tokens.add(readRelationOperator());
                    } else {
                        next();
                        tokens.add(new Token(TokenType.OP_UNARY, "!", lineNumber));
                    }
                    break;
                case '$':
                    next();
                    tokens.add(new Token(TokenType.TYPE_STRING, "$", lineNumber));
                    break;
                case '=':
                case '<':
                case '>':
                    tokens.add(readRelationOperator());
                    break;
                default:
                    throw new RuntimeException("Unexpected character: " + current + " at line " + lineNumber);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", lineNumber));
        return tokens;
    }

    // Пример использования
    public static void main(String[] args) {
        String testProgram =
                "(* Sample program *)\n" +
                        "for (  int j := X; i < 5; i := i + 1) do\n" ;

        var lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}