package com.kaspersky.task.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatcher implements Runnable {
    private final Path path;
    private final FileScanner scanner;
    private final WatchService watchService;

    public DirectoryWatcher(Path path, FileScanner scanner) throws IOException {
        this.path = path;
        this.scanner = scanner;
        this.watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
    }
    @Override
    public void run() {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    Path fullPath = path.resolve(fileName);

                    if (fullPath.toString().endsWith(".txt")) {
                        scanner.indexSingleFile(fullPath);
                    }
                }
                if (!key.reset()) break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Watcher error: " + e.getMessage());
        }
    }
}