package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.structure.BTreeProductStore;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class CsvLoadTask extends Task<LoadedCatalog> {
    private final Path file;

    public CsvLoadTask(Path file) {
        this.file = file;
    }

    @Override
    protected LoadedCatalog call() throws Exception {
        BTreeProductStore newTree = new BTreeProductStore();
        CsvService service = new CsvService();
        CsvLoadResult result = service.load(file, newTree, this::isCancelled, value -> updateProgress(value, 1.0));
        if (!result.isFinished()) {
            return null;
        }
        return new LoadedCatalog(newTree, result);
    }

}
