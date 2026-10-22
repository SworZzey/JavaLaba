package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.structure.NodeFillStats;
import com.sworzzey.storecatalog.structure.NodeView;
import com.sworzzey.storecatalog.structure.ProductStore;

import java.util.ArrayList;
import java.util.List;

public class CatalogService {
    private ProductStore storage;

    public CatalogService(ProductStore storage) {
        this.storage = storage;
    }

    public boolean add(Product product) {
        return storage.add(product);
    }

    public boolean remove(long article) {
        return storage.removeByArticle(article);
    }

    public Product find(long article) {
        return storage.findByArticle(article);
    }

    public int size() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
    }

    public NodeFillStats getFillStats() {
        return storage.getFillStats();
    }

    public List<Product> getAllSorted() {
        List<Product> sortedProducts = new ArrayList<Product>();
        storage.forEach(product -> sortedProducts.add(product));
        final List<Product> copy = List.copyOf(sortedProducts);
        return copy;
    }


    public List<Product> getRange(long from, long to) {
        List<Product> productRange = new ArrayList<Product>();
        storage.forEachInRange(from, to, product -> productRange.add(product));
        final List<Product> copy = List.copyOf(productRange);
        return copy;
    }

    public boolean replace(long oldArticle, Product newProduct) {
        Product foundProduct = storage.findByArticle(oldArticle);

        if (newProduct == null) {
            return false;
        }
        if (foundProduct == null) {
            return false;
        }
        if (oldArticle != newProduct.getArticle()) {
            if (storage.findByArticle(newProduct.getArticle()) != null) {
                return false;
            }
        }

        if(storage.removeByArticle(oldArticle)) {
            if (storage.add(newProduct)) {
                return true;
            } else {
                storage.add(foundProduct);
                return false;
            }
        } else {
            return false;
        }
    }


    public void replaceStorage(ProductStore newStorage) {
        if (newStorage == null) {
            throw new IllegalArgumentException("Передан null");
        }
        storage = newStorage;
    }

    public NodeView getSnapshot(int maxLevels) {
        return storage.snapshot(maxLevels);
    }
}
