package com.sworzzey.storecatalog.model;

public class DiscontinuedProduct extends Product{

    public DiscontinuedProduct(long article, String name, String category, double price, int stock) {
        super(article, name, category, price, stock);
    }


    @Override
    public boolean isEditable() {
        return false;
    }
}
