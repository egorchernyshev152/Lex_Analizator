import lexer.Lexer;
import lexer.Token;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String testProgram =
                "(* Sample program *)\n" +
                        "begin\n" +
                        "  x := 10;\n" +
                        "  y := 20.5;\n" +
                        "  if (x < y) writeln(\"x is less than y\") else writeln(\"x is greater or equal\");\n" +
                        "end\n";

        Lexer lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        Lexer.printTokensTable(tokens);
    }
}