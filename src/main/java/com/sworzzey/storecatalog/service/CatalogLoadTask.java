package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.serialization.CatalogData;
import com.sworzzey.storecatalog.serialization.CatalogMigrator;
import com.sworzzey.storecatalog.serialization.Serializer;
import com.sworzzey.storecatalog.structure.ProductStore;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class CatalogLoadTask extends Task<CatalogImportResult> {
    private final Path file;
    private final Serializer serializer;

    public CatalogLoadTask(Path file, Serializer serializer) {
        this.file = file;
        this.serializer = serializer;
    }

    protected CatalogImportResult call() throws Exception {
        CatalogData data = serializer.load(file);
        int sourceVersion = data.getSchemaVersion();
        CatalogMigrator.migrateToCurrent(data);
        ProductStore store = CatalogService.createStore(data);
        return new CatalogImportResult(store, sourceVersion);
    }
}
