package com.sworzzey.storecatalog.model;

public class Product implements Editable{
    private long article;
    private String name;
    private String category;
    private double price;
    private int stock;

    //конструктор
    public Product(long article, String name, String category, double price, int stock) {
        this.article = article;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    //геттеры и сеттеры
    //артикул
    public long getArticle() {
        return article;
    }

    public void setArticle(long article) {
        this.article = article;
    }

    //имя
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //категория
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    //цена
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    //остаток
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Product{" +
                "article=" + article +
                ", name=" + name +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                "}";
    }

    @Override
    public boolean isEditable() {
        return true;
    }
}
