package com.sworzzey.storecatalog.benchmark;

import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.service.ProductGenerator;
import com.sworzzey.storecatalog.structure.BTreeProductStore;

import java.util.TreeMap;

public class BTreeBenchmark {

    public static void main(String[] args) {
        test(10_000);
        test(100_000);
    }

    private static void test(int size) {
        System.out.println("SIZE = " + size);

        // Одинаковые товары для обеих структур, генерация вне замеров.
        Product[] products = generateProducts(size);
        BTreeProductStore tree = new BTreeProductStore();
        TreeMap<Long, Product> map = new TreeMap<>();

        // Вставка всех товаров.
        long start = System.nanoTime();
        for (Product product : products) {
            tree.add(product);
        }
        long end = System.nanoTime();
        System.out.println("BTree add: " + (end - start) / 1_000_000.0 + " ms");

        start = System.nanoTime();
        for (Product product : products) {
            map.put(product.getArticle(), product);
        }
        end = System.nanoTime();
        System.out.println("TreeMap add: " + (end - start) / 1_000_000.0 + " ms");

        if (tree.size() != size || map.size() != size) {
            throw new IllegalStateException("Неверный размер после вставки");
        }

        // По одному поиску для каждого товара.
        int treeFound = 0;
        start = System.nanoTime();
        for (Product product : products) {
            Product found = tree.findByArticle(product.getArticle());
            if (found != null && found.getArticle() == product.getArticle()) {
                treeFound++;
            }
        }
        end = System.nanoTime();
        System.out.println("BTree search: " + (end - start) / 1_000_000.0 + " ms");

        int mapFound = 0;
        start = System.nanoTime();
        for (Product product : products) {
            Product found = map.get(product.getArticle());
            if (found != null && found.getArticle() == product.getArticle()) {
                mapFound++;
            }
        }
        end = System.nanoTime();
        System.out.println("TreeMap search: " + (end - start) / 1_000_000.0 + " ms");

        if (treeFound != size || mapFound != size) {
            throw new IllegalStateException("Найдены не все товары");
        }

        // Один диапазонный скан с включёнными границами.
        long from = size / 4;
        long to = size / 2;
        // Массив позволяет изменять счётчик внутри лямбды.
        int[] treeCount = {0};
        start = System.nanoTime();
        tree.forEachInRange(from, to, product -> treeCount[0]++);
        end = System.nanoTime();
        System.out.println("BTree range: " + (end - start) / 1_000_000.0 + " ms");

        int mapCount = 0;
        start = System.nanoTime();
        for (Product product : map.subMap(from, true, to, true).values()) {
            mapCount++;
        }
        end = System.nanoTime();
        System.out.println("TreeMap range: " + (end - start) / 1_000_000.0 + " ms");
        System.out.println("Range [" + from + ", " + to + "]: "
                + treeCount[0] + " / " + mapCount + " products");

        if (treeCount[0] != to - from + 1 || mapCount != to - from + 1) {
            throw new IllegalStateException("Неверное количество товаров в диапазоне");
        }

        // Удаление всех товаров.
        int treeRemoved = 0;
        start = System.nanoTime();
        for (Product product : products) {
            if (tree.removeByArticle(product.getArticle())) {
                treeRemoved++;
            }
        }
        end = System.nanoTime();
        System.out.println("BTree remove: " + (end - start) / 1_000_000.0 + " ms");

        int mapRemoved = 0;
        start = System.nanoTime();
        for (Product product : products) {
            if (map.remove(product.getArticle()) != null) {
                mapRemoved++;
            }
        }
        end = System.nanoTime();
        System.out.println("TreeMap remove: " + (end - start) / 1_000_000.0 + " ms");

        if (treeRemoved != size || mapRemoved != size
                || tree.size() != 0 || !map.isEmpty()) {
            throw new IllegalStateException("Удалены не все товары");
        }
        System.out.println();
    }

    private static Product[] generateProducts(int size) {
        ProductGenerator generator = new ProductGenerator(12345L);
        long[] articles = generator.shuffledArticles(1, size);
        Product[] products = new Product[size];

        for (int i = 0; i < size; i++) {
            products[i] = generator.nextProduct(articles[i]);
        }
        return products;
    }
}
