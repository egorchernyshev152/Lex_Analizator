package lexer;

public class Token {
    public final TokenType type;
    public final String value;
    public final int lineNumber;

    public Token(TokenType type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public enum TokenType {
        // Ключевые слова
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

        // Типы данных
        TYPE_INT("Type"),
        TYPE_FLOAT("Type"),
        TYPE_STRING("Type"),

        // Операторы
        OP_RELATION("Operator"),
        OP_ADDITIVE("Operator"),
        OP_MULTIPLICATIVE("Operator"),
        OP_UNARY("Operator"),
        OP_ASSIGN("Operator"),

        // Разделители
        SEMICOLON("Delimiter"),
        COLON("Delimiter"),
        COMMA("Delimiter"),
        LPAREN("Delimiter"),
        RPAREN("Delimiter"),

        // Идентификаторы и литералы
        IDENTIFIER("Identifier"),
        INTEGER_LITERAL("Literal"),
        FLOAT_LITERAL("Literal"),
        STRING_LITERAL("Literal"),

        // Специальные токены
        COMMENT("Comment"),
        NEWLINE("Newline"),
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