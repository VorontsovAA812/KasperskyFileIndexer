package com.kaspersky.task.api;

import java.util.Set;

public interface Tokenizer {

    Set<String> tokenize(String content);
}