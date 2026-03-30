
import com.kaspersky.task.impl.SimpleTokenizer;
import com.kaspersky.task.model.IndexStorage;
import com.kaspersky.task.service.FileScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchEngineTest {

    private IndexStorage storage;
    private FileScanner scanner;

    @BeforeEach
    void setUp() {
        storage = new IndexStorage();
        scanner = new FileScanner(storage, new SimpleTokenizer());
    }

    @Test
    void testFullIndexingCycle(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "Hello Kaspersky World");

        scanner.scanDirectory(tempDir);
        Thread.sleep(600);

        Set<Path> results = storage.getFilesByWord("kaspersky");

        assertFalse(results.isEmpty(), "Индекс не должен быть пустым");
        assertTrue(results.stream().anyMatch(p -> p.getFileName().toString().equals("test.txt")),
                "Файл test.txt должен быть найден");
    }

    @Test
    void testTokenizerLogic() {
        SimpleTokenizer tokenizer = new SimpleTokenizer();
        Set<String> tokens = tokenizer.tokenize("Java, Spring; Hibernate!");

        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("spring"));
        assertTrue(tokens.contains("hibernate"));
        assertEquals(3, tokens.size());
    }
}