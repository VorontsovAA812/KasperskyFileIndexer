package com.kaspersky.task.service;

import com.kaspersky.task.api.Tokenizer;
import com.kaspersky.task.exception.IndexingException;
import com.kaspersky.task.model.IndexStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public class FileScanner {
    private final IndexStorage storage;
    private final Tokenizer tokenizer;

    public FileScanner(IndexStorage storage, Tokenizer tokenizer) {
        this.storage = storage;
        this.tokenizer = tokenizer;
    }

    public void scanDirectory(Path rootPath) {
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IndexingException("Указанный путь не существует или не является директорией: " + rootPath, null);
        }

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(this::indexFile);
        } catch (IOException e) {
            throw new IndexingException("Ошибка при обходе дерева файлов", e);
        }
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
}