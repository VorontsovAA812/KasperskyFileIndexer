package com.kaspersky.task;


import com.kaspersky.task.api.Tokenizer;
import com.kaspersky.task.impl.SimpleTokenizer;
import com.kaspersky.task.model.IndexStorage;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        IndexStorage storage = new IndexStorage();
        Tokenizer tokenizer = new SimpleTokenizer();

        String text = "Java is fast. Java is multithreaded.";
        Path fakePath = Path.of("test.txt");

        Set<String> words = tokenizer.tokenize(text);
        for (String word : words) {
            storage.addWord(word, fakePath);
        }

        System.out.println("Где есть слово 'java'?: " + storage.getFilesByWord("java"));
        System.out.println("Где есть слово 'fast'?: " + storage.getFilesByWord("fast"));
    }
}