package lexer;

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

    private boolean isEOF() {
        return position >= input.length();
    }

    private char peek() {
        return isEOF() ? '\0' : input.charAt(position);
    }

    private char next() {
        if (isEOF()) return '\0';
        char c = input.charAt(position++);
        if (c == '\n') lineNumber++;
        return c;
    }

    private void skipWhitespace() {
        while (!isEOF() && Character.isWhitespace(peek()) && peek() != '\n') {
            next();
        }
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private Token readIdentifierOrKeyword() {
        int start = position;
        while (!isEOF() && isIdentifierPart(peek())) {
            next();
        }
        String value = input.substring(start, position);

        switch (value.toLowerCase()) {
            case "begin": return new Token(Token.TokenType.KEYWORD_BEGIN, value, lineNumber);
            case "end": return new Token(Token.TokenType.KEYWORD_END, value, lineNumber);
            case "if": return new Token(Token.TokenType.KEYWORD_IF, value, lineNumber);
            case "else": return new Token(Token.TokenType.KEYWORD_ELSE, value, lineNumber);
            case "for": return new Token(Token.TokenType.KEYWORD_FOR, value, lineNumber);
            case "to": return new Token(Token.TokenType.KEYWORD_TO, value, lineNumber);
            case "step": return new Token(Token.TokenType.KEYWORD_STEP, value, lineNumber);
            case "next": return new Token(Token.TokenType.KEYWORD_NEXT, value, lineNumber);
            case "while": return new Token(Token.TokenType.KEYWORD_WHILE, value, lineNumber);
            case "readln": return new Token(Token.TokenType.KEYWORD_READLN, value, lineNumber);
            case "writeln": return new Token(Token.TokenType.KEYWORD_WRITELN, value, lineNumber);
            default: return new Token(Token.TokenType.IDENTIFIER, value, lineNumber);
        }
    }

    private Token readNumber() {
        int start = position;
        boolean isFloat = false;

        while (!isEOF() && isDigit(peek())) {
            next();
        }

        if (!isEOF() && peek() == '.') {
            isFloat = true;
            next();
            while (!isEOF() && isDigit(peek())) {
                next();
            }
        }

        String value = input.substring(start, position);
        return isFloat
                ? new Token(Token.TokenType.FLOAT_LITERAL, value, lineNumber)
                : new Token(Token.TokenType.INTEGER_LITERAL, value, lineNumber);
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        next(); // Пропускаем открывающую кавычку

        while (!isEOF() && peek() != '"') {
            if (peek() == '\\') {
                next();
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
        return new Token(Token.TokenType.STRING_LITERAL, sb.toString(), lineNumber);
    }

    private Token readComment() {
        int startLine = lineNumber;
        next(); // Пропускаем '('

        if (isEOF() || peek() != '*') {
            return new Token(Token.TokenType.LPAREN, "(", startLine);
        }

        next(); // Пропускаем '*'
        StringBuilder sb = new StringBuilder();

        while (!isEOF()) {
            if (peek() == '*' && position + 1 < input.length() && input.charAt(position + 1) == ')') {
                next(); // Пропускаем '*'
                next(); // Пропускаем ')'
                return new Token(Token.TokenType.COMMENT, sb.toString(), startLine);
            }
            sb.append(next());
        }

        throw new RuntimeException("Unterminated comment starting at line " + startLine);
    }

    private Token readRelationOperator() {
        char firstChar = next();

        switch (firstChar) {
            case '=':
                if (peek() == '=') {
                    next();
                    return new Token(Token.TokenType.OP_RELATION, "==", lineNumber);
                }
                return new Token(Token.TokenType.OP_RELATION, "=", lineNumber);
            case '!':
                if (peek() == '=') {
                    next();
                    return new Token(Token.TokenType.OP_RELATION, "!=", lineNumber);
                }
                return new Token(Token.TokenType.OP_UNARY, "!", lineNumber);
            case '<':
                if (peek() == '=') {
                    next();
                    return new Token(Token.TokenType.OP_RELATION, "<=", lineNumber);
                }
                return new Token(Token.TokenType.OP_RELATION, "<", lineNumber);
            case '>':
                if (peek() == '=') {
                    next();
                    return new Token(Token.TokenType.OP_RELATION, ">=", lineNumber);
                }
                return new Token(Token.TokenType.OP_RELATION, ">", lineNumber);
            default:
                throw new RuntimeException("Unexpected character: " + firstChar);
        }
    }

    private Token readAssignmentOperator() {
        char firstChar = next();
        if (firstChar == ':' && peek() == '=') {
            next();
            return new Token(Token.TokenType.OP_ASSIGN, ":=", lineNumber);
        }
        throw new RuntimeException("Unexpected character: " + firstChar);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isEOF()) {
            char current = peek();

            if (Character.isWhitespace(current)) {
                if (current == '\n') {
                    tokens.add(new Token(Token.TokenType.NEWLINE, "\\n", lineNumber));
                    next();
                } else {
                    skipWhitespace();
                }
                continue;
            }

            if (current == '(' && position + 1 < input.length() && input.charAt(position + 1) == '*') {
                tokens.add(readComment());
                continue;
            }

            if (isIdentifierStart(current)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            if (isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            if (current == '"') {
                tokens.add(readString());
                continue;
            }

            switch (current) {
                case ';':
                    next();
                    tokens.add(new Token(Token.TokenType.SEMICOLON, ";", lineNumber));
                    break;
                case ':':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                        tokens.add(readAssignmentOperator());
                    } else {
                        next();
                        tokens.add(new Token(Token.TokenType.COLON, ":", lineNumber));
                    }
                    break;
                case ',':
                    next();
                    tokens.add(new Token(Token.TokenType.COMMA, ",", lineNumber));
                    break;
                case '(':
                    next();
                    tokens.add(new Token(Token.TokenType.LPAREN, "(", lineNumber));
                    break;
                case ')':
                    next();
                    tokens.add(new Token(Token.TokenType.RPAREN, ")", lineNumber));
                    break;
                case '+':
                case '-':
                    next();
                    tokens.add(new Token(Token.TokenType.OP_ADDITIVE, String.valueOf(current), lineNumber));
                    break;
                case '|':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '|') {
                        next(); next();
                        tokens.add(new Token(Token.TokenType.OP_ADDITIVE, "||", lineNumber));
                    } else {
                        throw new RuntimeException("Unexpected character: " + current);
                    }
                    break;
                case '*':
                case '/':
                    next();
                    tokens.add(new Token(Token.TokenType.OP_MULTIPLICATIVE, String.valueOf(current), lineNumber));
                    break;
                case '&':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '&') {
                        next(); next();
                        tokens.add(new Token(Token.TokenType.OP_MULTIPLICATIVE, "&&", lineNumber));
                    } else {
                        throw new RuntimeException("Unexpected character: " + current);
                    }
                    break;
                case '%':
                    next();
                    tokens.add(new Token(Token.TokenType.TYPE_INT, "%", lineNumber));
                    break;
                case '!':
                    if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                        tokens.add(readRelationOperator());
                    } else {
                        next();
                        tokens.add(new Token(Token.TokenType.OP_UNARY, "!", lineNumber));
                    }
                    break;
                case '$':
                    next();
                    tokens.add(new Token(Token.TokenType.TYPE_STRING, "$", lineNumber));
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

        tokens.add(new Token(Token.TokenType.EOF, "", lineNumber));
        return tokens;
    }

    public static void printTokensTable(List<Token> tokens) {
        System.out.println("┌───────┬───────────────────────┬───────────────────────┐");
        System.out.println("│ Line  │ Lexeme                │ Type                  │");
        System.out.println("├───────┼───────────────────────┼───────────────────────┤");

        for (Token token : tokens) {
            String lineNo = token.lineNumber > 0 ? String.valueOf(token.lineNumber) : "";
            String lexeme = token.value
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\r", "\\r");

            if (lexeme.length() > 20) {
                lexeme = lexeme.substring(0, 17) + "...";
            }

            String type = token.type.getCategory() + " (" + token.type + ")";
            if (type.length() > 20) {
                type = type.substring(0, 17) + "...";
            }

            System.out.printf("│ %-5s │ %-21s │ %-21s │%n", lineNo, lexeme, type);
        }

        System.out.println("└───────┴───────────────────────┴───────────────────────┘");
    }
}