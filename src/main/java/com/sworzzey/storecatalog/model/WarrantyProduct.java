package com.sworzzey.storecatalog.model;

public class WarrantyProduct extends Product{
    private int warrantyMonths;

    public WarrantyProduct(long article, String name, String category, double price, int stock, int warrantyMonths) {
        super(article, name, category, price, stock);
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String toString() {
        return super.toString() + ", WarrantyMonths=" + warrantyMonths;
    }
}
