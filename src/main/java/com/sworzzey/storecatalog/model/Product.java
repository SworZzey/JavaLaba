package com.sworzzey.storecatalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Product.class, name = "PRODUCT"),
    @JsonSubTypes.Type(value = WarrantyProduct.class, name = "WARRANTY"),
    @JsonSubTypes.Type(value = DiscontinuedProduct.class, name = "DISCONTINUED")
})
public class Product implements Editable, Serializable {
    private long article;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String note = "";
    private int rating = 0;
    private static final long serialVersionUID = 1L;

    //конструктор
    public Product(long article, String name, String category, double price, int stock) {
        this.article = article;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public Product() {}

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

    //записка
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        if (note == null) {
            throw new IllegalArgumentException("Заметка не может быть null");
        }
        this.note = note;
    }

    //рейтинг
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (!(rating >= 0 && rating <= 5)) {
            throw new IllegalArgumentException("Рейтинг должен быть от 0 до 5");
        }
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Product{" +
                "article=" + article +
                ", name=" + name +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                ", note=" + note +
                ", rating=" + rating +
                "}";
    }

    @JsonIgnore
    @Override
    public boolean isEditable() {
        return true;
    }
}
