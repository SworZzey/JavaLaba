package com.sworzzey.storecatalog.service;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import com.sworzzey.storecatalog.structure.ProductStore;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class ProductGenerator {
    private final Random random;
    //шансы создания типа продукта
    private static final int PRODUCT_CHANCE = 70;
    private static final int WARRANTY_CHANCE = 20;

    private static final int PROGRESS_STEP = 100;

    private static final String[] categoryArray = {"Electronics", "Home", "Kitchen", "Office", "Sports", "Garden", "Stationery"};
    private static final String[] firstName = {"Wireless", "Smart", "Classic", "Compact", "Portable"};
    private static final String[] secondName = {"Mouse", "Lamp", "Bottle", "Keyboard", "Speaker", "Mug"};
    private static final int[] warrantyPeriod = {6, 12, 24, 36};


    public ProductGenerator(long seed) {
        random = new Random(seed);
    }

    public Product nextProduct(long article) {
        int randomChance = random.nextInt(100);

        String category = categoryArray[random.nextInt(categoryArray.length)];

        String name = firstName[random.nextInt(firstName.length)] + secondName[random.nextInt(secondName.length)];

        int warranty = warrantyPeriod[random.nextInt(warrantyPeriod.length)];

        int priceInCents = random.nextInt(100, 5_000_001);
        double price = priceInCents / 100.0;

        int stock = random.nextInt(501);

        if (randomChance < PRODUCT_CHANCE) {
            return new Product(article, name, category, price, stock);
        } else if (randomChance < PRODUCT_CHANCE+WARRANTY_CHANCE) {
            return new WarrantyProduct(article, name, category, price, stock, warranty);
        } else {
            return new DiscontinuedProduct(article, name, category, price, stock);
        }
    }

    public long[] shuffledArticles(long firstArticle, int count) {
        if (count <= 0) {
            return new long[0];
        }
        long[] articles = new long[count];
        for (int i = 0; i < count; i++) {
            articles[i] = firstArticle+i;
        }
        for (int i = articles.length-1; i > 0; i--) {
            int j = random.nextInt(i+1);
            long trial = articles[i];
            articles[i] = articles[j];
            articles[j] = trial;
        }
        return articles;
    }


    public boolean fill(ProductStore target, long firstArticle, int count, BooleanSupplier cancelled, IntConsumer progress) {
        if (count == 0) {
            progress.accept(0);
            return true;
        }
        if (count < 0) {
            throw new IllegalArgumentException("Только положительные значения");
        }
        int addedProduct = 0;
        long[] articles = shuffledArticles(firstArticle, count);
        for (int i = 0; i < articles.length; i++) {
            if (cancelled.getAsBoolean()) {
                return false;
            }
            Product newProduct = nextProduct(articles[i]);

            if(!target.add(newProduct)) {
                throw new IllegalStateException("Не удалось добавить " + newProduct.getArticle());
            }
            addedProduct++;
            if (addedProduct % PROGRESS_STEP == 0) {
                progress.accept(addedProduct);
            }
        }
        progress.accept(addedProduct);
        return true;
    }
}
