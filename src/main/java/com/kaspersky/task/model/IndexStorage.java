package com.kaspersky.task.model;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IndexStorage {
    private final Map<String, Set<Path>> data = new ConcurrentHashMap<>();

    public void addWord(String word, Path path) {
        data.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet()).add(path);
    }

    public Set<Path> getFilesByWord(String word) {
        return data.getOrDefault(word.toLowerCase(), Collections.emptySet());
    }

    public void removeFileReferences(Path path) {
        data.values().forEach(set -> set.remove(path));
    }
}