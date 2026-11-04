package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.serialization.CatalogData;
import com.sworzzey.storecatalog.serialization.Serializer;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class CatalogSaveTask extends Task<Void> {
    private final Path file;
    private final Serializer serializer;
    private final CatalogService catalogService;

    public CatalogSaveTask(Path file, Serializer serializer, CatalogService catalogService) {
        this.file = file;
        this.serializer = serializer;
        this.catalogService = catalogService;
    }

    protected Void call() throws Exception {
        CatalogData data = catalogService.exportData();
        serializer.save(data, file);
        return null;
    }
}
