package com.sworzzey.storecatalog.model;
import java.util.ArrayList;
import java.util.List;

public class StoreCatalog {
    private final List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
    }

    public List<Product> getProducts() {
        return products;
    }

    public void deleteProduct(int ind) {
        if (products.size() > ind && ind >= 0) {
            products.remove(ind);
        }
    }
}
