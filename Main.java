import lexer.Lexer;
import lexer.Token;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Проверяем, передан ли аргумент (путь к файлу)
        if (args.length == 0) {
            System.err.println("Ошибка: Укажите путь к файлу с исходным кодом.");
            System.err.println("Пример: java Main program.txt");
            return;
        }

        String filePath = args[0];
        String testProgram;

        try {
            // Читаем весь файл в строку
            testProgram = Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return;
        }

        Lexer lexer = new Lexer(testProgram);
        List<Token> tokens = lexer.tokenize();

        Lexer.printTokensTable(tokens);
    }
}