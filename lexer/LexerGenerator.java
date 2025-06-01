package lexer;

import java.util.*;
import java.util.regex.*;

public class LexerGenerator {

    public static class TokenDef {
        public final String name;
        public final String regex;

        public TokenDef(String name, String regex) {
            this.name = name;
            this.regex = regex;
        }
    }

    private final List<TokenDef> tokenDefs = new ArrayList<>();
    private final Set<String> skipTokens = new HashSet<>(); // токены, которые не возвращаем (пробелы, комментарии)

    public void addToken(String name, String regex, boolean skip) {
        tokenDefs.add(new TokenDef(name, regex));
        if (skip) skipTokens.add(name);
    }

    public Lexer generateLexer() {
        return new Lexer(tokenDefs, skipTokens);
    }

    public static class Token {
        public final String type;
        public final String value;
        public final int line;

        public Token(String type, String value, int line) {
            this.type = type;
            this.value = value;
            this.line = line;
        }

        @Override
        public String toString() {
            return String.format("Token(%s, '%s', line=%d)", type, value, line);
        }
    }

    public static class Lexer {
        private final List<TokenDef> tokenDefs;
        private final Set<String> skipTokens;

        public Lexer(List<TokenDef> tokenDefs, Set<String> skipTokens) {
            this.tokenDefs = tokenDefs;
            this.skipTokens = skipTokens;
        }

        public List<Token> tokenize(String input) {
            List<Token> tokens = new ArrayList<>();
            int pos = 0;
            int line = 1;

            while (pos < input.length()) {
                boolean matched = false;
                String remaining = input.substring(pos);

                for (TokenDef def : tokenDefs) {
                    Pattern p = Pattern.compile("^" + def.regex, Pattern.DOTALL);
                    Matcher m = p.matcher(remaining);

                    if (m.find()) {
                        String lexeme = m.group();
                        // Подсчёт строк в лексеме (для корректного line)
                        int newlines = countNewlines(lexeme);

                        if (!skipTokens.contains(def.name)) {
                            tokens.add(new Token(def.name, lexeme, line));
                        }
                        line += newlines;
                        pos += lexeme.length();
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    throw new RuntimeException(
                            "Unexpected character at position " + pos + " (line " + line + "): '" + input.charAt(pos) + "'");
                }
            }
            return tokens;
        }

        private int countNewlines(String text) {
            int count = 0;
            for (char c : text.toCharArray()) {
                if (c == '\n') count++;
            }
            return count;
        }
    }

    // Пример использования
    public static void main(String[] args) {
        LexerGenerator gen = new LexerGenerator();

        // Добавляем токены: название, регулярка, пропускать ли в итоговом списке
        gen.addToken("WHITESPACE", "[ \\t\\r\\f]+", true);
        gen.addToken("NEWLINE", "\\n", true);
        gen.addToken("COMMENT", "//.*|/\\*(.|\\R)*?\\*/", true);

        gen.addToken("STRING_LITERAL", "\"(\\\\.|[^\"\\\\])*\"", false);

        gen.addToken("NUMBER", "\\d+(\\.\\d+)?", false);
        gen.addToken("IDENTIFIER", "[a-zA-Z_][a-zA-Z0-9_]*", false);

        gen.addToken("ASSIGN", ":=", false);
        gen.addToken("LESS", "<", false);
        gen.addToken("GREATER", ">", false);
        gen.addToken("LESSEQ", "<=", false);
        gen.addToken("GREATEREQ", ">=", false);
        gen.addToken("EQUAL", "==", false);
        gen.addToken("NOTEQUAL", "!=", false);

        gen.addToken("PLUS", "\\+", false);
        gen.addToken("MINUS", "-", false);
        gen.addToken("MULT", "\\*", false);
        gen.addToken("DIV", "/", false);

        gen.addToken("LPAREN", "\\(", false);
        gen.addToken("RPAREN", "\\)", false);
        gen.addToken("SEMICOLON", ";", false);

        Lexer lexer = gen.generateLexer();

        String code = "begin\n" +
                "  x := 10;\n" +
                "  y := 20.5;\n" +
                "  if (x < y) writeln(\"x is less than y\") else writeln(\"x is greater or equal\");\n" +
                "end";

        try {
            List<Token> tokens = lexer.tokenize(code);
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (RuntimeException e) {
            System.err.println("Ошибка лексического анализа: " + e.getMessage());
        }
    }
}
