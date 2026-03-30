package com.kaspersky.task;

import com.kaspersky.task.api.Tokenizer;
import com.kaspersky.task.impl.SimpleTokenizer;
import com.kaspersky.task.model.IndexStorage;
import com.kaspersky.task.service.FileScanner;
import com.kaspersky.task.exception.IndexingException;
import com.kaspersky.task.service.DirectoryWatcher;

import java.nio.file.Path;
import java.util.Set;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Ошибка: Не указан путь к директории для сканирования.");
            System.out.println("Пример: java -cp target/classes com.kaspersky.task.Main /home/user/texts");
            return;
        }

        IndexStorage storage = new IndexStorage();
        Tokenizer tokenizer = new SimpleTokenizer();
        FileScanner scanner = new FileScanner(storage, tokenizer);
        Path directoryToScan = Path.of(args[0]);

        try {
            System.out.println("=== Система полнотекстового поиска ===");
            System.out.println("Сканирование: " + directoryToScan.toAbsolutePath());

            scanner.scanDirectory(directoryToScan);
            DirectoryWatcher watcher = new DirectoryWatcher(directoryToScan, scanner);
            Thread watcherThread = new Thread(watcher);
            watcherThread.setDaemon(true);
            watcherThread.start();

            System.out.println("Индексация завершена. Включено авто-обновление (WatchService).");

            runSearchLoop(storage);
            scanner.stop();

        } catch (IndexingException | java.io.IOException e) {
            System.err.println("Ошибка конфигурации или доступа: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Непредвиденный системный сбой", e);
            System.exit(1);
        }
    }

    private static void runSearchLoop(IndexStorage storage) {
        try (Scanner console = new Scanner(System.in)) {
            System.out.println("\nВведите слово для поиска (или 'exit' для выхода):");
            while (true) {
                System.out.print("> ");
                if (!console.hasNextLine()) break;

                String query = console.nextLine().trim();
                if (query.equalsIgnoreCase("exit")) break;
                if (query.isEmpty()) continue;

                Set<Path> results = storage.getFilesByWord(query);
                if (results.isEmpty()) {
                    System.out.println("Ничего не найдено.");
                } else {
                    System.out.println("Найдено в файлах (" + results.size() + "):");
                    results.forEach(p -> System.out.println("  - " + p.getFileName()));
                }
            }
        }
    }
}