package com.kaspersky.task.impl;


import com.kaspersky.task.api.Tokenizer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleTokenizer implements Tokenizer {
    @Override
    public Set<String> tokenize(String content) {
        if (content == null || content.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(content.toLowerCase().split("\\W+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
    }
}
