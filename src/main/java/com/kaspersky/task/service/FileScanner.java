package com.kaspersky.task.service;

import com.kaspersky.task.api.Tokenizer;
import com.kaspersky.task.exception.IndexingException;
import com.kaspersky.task.model.IndexStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileScanner {
    private final IndexStorage storage;
    private final Tokenizer tokenizer;

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    public FileScanner(IndexStorage storage, Tokenizer tokenizer) {
        this.storage = storage;
        this.tokenizer = tokenizer;
    }

    public void scanDirectory(Path rootPath) {
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IndexingException("Путь не найден: " + rootPath, null);
        }

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(this::indexSingleFile);
            Thread.sleep(1000);

        } catch (IOException e) {
            throw new IndexingException("Ошибка при обходе дерева файлов", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void indexSingleFile(Path path) {
        executor.submit(() -> indexFile(path));
    }

    private void indexFile(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                Set<String> tokens = tokenizer.tokenize(line);
                tokens.forEach(word -> storage.addWord(word, path));
            });
        } catch (IOException e) {
            throw new IndexingException("Не удалось прочитать файл: " + path, e);
        }
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}