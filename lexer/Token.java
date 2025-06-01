package lexer;

/**
 * Класс Token представляет одну лексему.
 */
public class Token {
    public final TokenType type;
    public final String value;
    public final int lineNumber;
    public final int index;       // порядковый номер токена среди всех токенов

    public Token(TokenType type, String value, int lineNumber, int index, int tokenIndex) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;

        this.index = index;
    }

    public enum TokenType {
        // Категория "Keyword"
        KEYWORD_BEGIN("Keyword"),
        KEYWORD_END("Keyword"),
        KEYWORD_IF("Keyword"),
        KEYWORD_ELSE("Keyword"),
        KEYWORD_FOR("Keyword"),
        KEYWORD_TO("Keyword"),
        KEYWORD_STEP("Keyword"),
        KEYWORD_NEXT("Keyword"),
        KEYWORD_WHILE("Keyword"),
        KEYWORD_READLN("Keyword"),
        KEYWORD_WRITELN("Keyword"),
        KEYWORD_IS("Keyword"),
        KEYWORD_LESS("Keyword"),
        KEYWORD_THAN("Keyword"),
        KEYWORD_GREATER("Keyword"),
        KEYWORD_OR("Keyword"),
        KEYWORD_EQUAL("Keyword"),

        IDENTIFIER("Identifier"),
        INTEGER_LITERAL("Literal"),
        FLOAT_LITERAL("Literal"),
        STRING_LITERAL("Literal"),

        OP_ASSIGN("Operator"),        // :=
        OP_RELATION("Operator"),      // <=, >=, <>, <, >, =
        OP_ADDITIVE("Operator"),      // +, -
        OP_MULTIPLICATIVE("Operator"),// *, /, and
        OP_UNARY("Operator"),         // not, !

        SEMICOLON("Delimiter"),       // ;
        COLON("Delimiter"),           // :
        COMMA("Delimiter"),           // ,
        LPAREN("Delimiter"),          // (
        RPAREN("Delimiter"),          // )

        WHITESPACE("Whitespace"),
        NEWLINE("Newline"),
        COMMENT("Comment"),

        EOF("EOF");

        private final String category;

        TokenType(String category) {
            this.category = category;
        }

        public String getCategory() {
            return category;
        }
    }
}
