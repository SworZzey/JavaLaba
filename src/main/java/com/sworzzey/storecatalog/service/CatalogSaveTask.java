package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.serialization.CatalogData;
import com.sworzzey.storecatalog.serialization.CatalogDowngrader;
import com.sworzzey.storecatalog.serialization.Serializer;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class CatalogSaveTask extends Task<Void> {
    private final Path file;
    private final Serializer serializer;
    private final CatalogService catalogService;
    private final int targetVersion;

    public CatalogSaveTask(Path file, Serializer serializer, CatalogService catalogService, int targetVersion) {
        this.file = file;
        this.serializer = serializer;
        this.catalogService = catalogService;
        this.targetVersion = targetVersion;
    }

    protected Void call() throws Exception {
        CatalogData data = catalogService.exportData();
        CatalogData downgradedData = CatalogDowngrader.downgrade(data, targetVersion);
        serializer.save(downgradedData, file);
        return null;
    }
}
