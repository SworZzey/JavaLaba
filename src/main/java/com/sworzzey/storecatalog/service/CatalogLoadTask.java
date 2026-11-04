package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.serialization.CatalogData;
import com.sworzzey.storecatalog.serialization.Serializer;
import com.sworzzey.storecatalog.structure.ProductStore;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class CatalogLoadTask extends Task<ProductStore> {
    private final Path file;
    private final Serializer serializer;

    public CatalogLoadTask(Path file, Serializer serializer) {
        this.file = file;
        this.serializer = serializer;
    }

    protected ProductStore call() throws Exception {
        CatalogData data = serializer.load(file);
        return CatalogService.createStore(data);
    }
}
