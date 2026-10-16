package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.structure.BTreeProductStore;
import com.sworzzey.storecatalog.structure.ProductStore;
import javafx.concurrent.Task;

public class GenerateCatalogTask extends Task<ProductStore> {
    private final int count;
    private final long seed;
    private final long firstArticle;

    public GenerateCatalogTask(int count, long seed, long firstArticle) {
        this.count = count;
        this.seed = seed;
        this.firstArticle = firstArticle;
    }

    @Override
    protected ProductStore call() {
        BTreeProductStore newTree = new BTreeProductStore();
        ProductGenerator newGenerator = new ProductGenerator(seed);
        boolean complete = newGenerator.fill(newTree, firstArticle, count, this::isCancelled, amount -> updateProgress(amount, count));
        if(!complete) {
            return null;
        }
        return newTree;
    }
}
