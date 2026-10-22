package com.sworzzey.storecatalog.structure;

import com.sworzzey.storecatalog.model.Product;

import java.util.function.Consumer;

public class BTreeProductStore implements ProductStore{
    private final BTreeStore<Product> tree =
            new BTreeStore<>(Product::getArticle);



    @Override
    public boolean add(Product product) {
        return tree.add(product);
    }

    @Override
    public boolean add(Product product, VisitTrace trace) {
        return tree.add(product, trace);
    }

    @Override
    public Product findByArticle(long article) {
        return tree.findByArticle(article);
    }

    @Override
    public Product findByArticle(long article, VisitTrace trace) {
        return tree.findByArticle(article, trace);
    }

    @Override
    public boolean removeByArticle(long article) {
        return tree.removeByArticle(article);
    }

    @Override
    public int size() {
        return tree.size();
    }

    @Override
    public void forEach(Consumer<Product> action) {
        tree.forEach(action);
    }

    @Override
    public void forEachInRange(long from, long to, Consumer<Product> action) {
        tree.forEachInRange(from, to, action);
    }

    @Override
    public NodeFillStats getFillStats() {
        return tree.getFillStats();
    }

    @Override
    public void clear() {
        tree.clear();
    }

    @Override
    public NodeView snapshot(int maxLevels) {
        return tree.snapshot(maxLevels);
    }

}
