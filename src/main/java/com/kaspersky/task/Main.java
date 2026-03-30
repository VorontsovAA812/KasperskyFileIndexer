package com.kaspersky.task;

import com.kaspersky.task.api.Tokenizer;
import com.kaspersky.task.impl.SimpleTokenizer;
import com.kaspersky.task.model.IndexStorage;
import com.kaspersky.task.service.FileScanner;
import com.kaspersky.task.exception.IndexingException;

import java.nio.file.Path;
import java.util.Set;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Ошибка: Не указан путь к директории для сканирования.");
            System.out.println("Использование: java -jar search.jar <путь_к_папке>");
            return;
        }

        IndexStorage storage = new IndexStorage();
        Tokenizer tokenizer = new SimpleTokenizer();
        FileScanner scanner = new FileScanner(storage, tokenizer);

        Path directoryToScan = Path.of(args[0]);

        try {
            System.out.println("=== Система полнотекстового поиска ===");
            System.out.println("Целевая директория: " + directoryToScan.toAbsolutePath());

            long start = System.currentTimeMillis();
            scanner.scanDirectory(directoryToScan);
            long end = System.currentTimeMillis();

            System.out.printf("Индексация завершена успешно за %d мс.\n", (end - start));

            runSearchLoop(storage);

        } catch (IndexingException e) {
            System.err.println("Ошибка индексации: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Причина: " + e.getCause().getMessage());
            }
        }
    }

    private static void runSearchLoop(IndexStorage storage) {
        try (Scanner console = new Scanner(System.in)) {
            System.out.println("\nВведите слово для поиска (или 'exit' для выхода):");
            while (true) {
                System.out.print("> ");
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