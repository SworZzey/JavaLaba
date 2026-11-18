package com.sworzzey.storecatalog.serialization;

import com.sworzzey.storecatalog.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CatalogMigratorTest {
    @TempDir
    private Path tempDir;

    @Test
    public void binFromV1ToV3() throws IOException {
        Path path = tempDir.resolve("catalog-v1.bin");
        try (InputStream input = getClass().getResourceAsStream("/versions/catalog-v1.bin")) {
            assertNotNull(input, "Не найден файл");
            Files.copy(input, path);
        }
        BinarySerializer serializer = new BinarySerializer();
        CatalogData data = serializer.load(path);
        if (data.getSchemaVersion() != 1) {
            throw new IOException("Версия файла не равна 1");
        }
        int productsNumber = data.getProducts().length;
        long firstArticle = data.getProducts()[0].getArticle();
        String firstName = data.getProducts()[0].getName();
        double firstPrice = data.getProducts()[0].getPrice();
        CatalogMigrator.migrateToCurrent(data);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, data.getSchemaVersion());
        assertEquals(productsNumber, data.getProducts().length);
        assertEquals(firstArticle, data.getProducts()[0].getArticle());
        assertEquals(firstName, data.getProducts()[0].getName());
        assertEquals(firstPrice, data.getProducts()[0].getPrice());
        for (int i = 0; i < data.getProducts().length; i++) {
            assertEquals("", data.getProducts()[i].getNote());
            assertEquals(0, data.getProducts()[i].getRating());
        }
    }

    @Test
    public void jsonFromV1ToV3() throws IOException {
        Path path = tempDir.resolve("catalog-v1.json");
        try (InputStream input = getClass().getResourceAsStream("/versions/catalog-v1.json")) {
            assertNotNull(input, "Не найден файл");
            Files.copy(input, path);
        }
        JsonSerializer serializer = new JsonSerializer();
        CatalogData data = serializer.load(path);
        if (data.getSchemaVersion() != 1) {
            throw new IOException("Версия файла не равна 1");
        }
        int productsNumber = data.getProducts().length;
        long firstArticle = data.getProducts()[0].getArticle();
        String firstName = data.getProducts()[0].getName();
        double firstPrice = data.getProducts()[0].getPrice();
        CatalogMigrator.migrateToCurrent(data);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, data.getSchemaVersion());
        assertEquals(productsNumber, data.getProducts().length);
        assertEquals(firstArticle, data.getProducts()[0].getArticle());
        assertEquals(firstName, data.getProducts()[0].getName());
        assertEquals(firstPrice, data.getProducts()[0].getPrice());
        for (int i = 0; i < data.getProducts().length; i++) {
            assertEquals("", data.getProducts()[i].getNote());
            assertEquals(0, data.getProducts()[i].getRating());
        }
    }

    @Test
    public void binFromV2ToV3() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        product.setNote("Проверить упаковку");
        Product[] products = {product};
        CatalogData catalogData = new CatalogData(2, products);
        Path path = tempDir.resolve("catalog-v2.bin");
        BinarySerializer serializer = new BinarySerializer();
        serializer.save(catalogData, path);
        CatalogData loaded = serializer.load(path);
        if (loaded.getSchemaVersion() != 2) {
            throw new IOException("Версия файла не равна 2");
        }
        CatalogMigrator.migrateToCurrent(loaded);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, loaded.getSchemaVersion());
        assertEquals(1, loaded.getProducts()[0].getArticle());
        assertEquals("pillow", loaded.getProducts()[0].getName());
        assertEquals("house", loaded.getProducts()[0].getCategory());
        assertEquals(10, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
        assertEquals("Проверить упаковку", loaded.getProducts()[0].getNote());
        assertEquals(0, loaded.getProducts()[0].getRating());
    }


    @Test
    public void jsonFromV2ToV3() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        product.setNote("Проверить упаковку");
        Product[] products = {product};
        CatalogData catalogData = new CatalogData(2, products);
        Path path = tempDir.resolve("catalog-v2.json");
        JsonSerializer serializer = new JsonSerializer();
        serializer.save(catalogData, path);
        CatalogData loaded = serializer.load(path);
        if (loaded.getSchemaVersion() != 2) {
            throw new IOException("Версия файла не равна 2");
        }
        CatalogMigrator.migrateToCurrent(loaded);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, loaded.getSchemaVersion());
        assertEquals(1, loaded.getProducts()[0].getArticle());
        assertEquals("pillow", loaded.getProducts()[0].getName());
        assertEquals("house", loaded.getProducts()[0].getCategory());
        assertEquals(10, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
        assertEquals("Проверить упаковку", loaded.getProducts()[0].getNote());
        assertEquals(0, loaded.getProducts()[0].getRating());
    }

    @Test
    public void binCurrentVersionKeepsData() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        product.setNote("Проверить упаковку");
        product.setRating(4);
        Product[] products = {product};
        CatalogData catalogData = new CatalogData(CatalogData.CURRENT_SCHEMA_VERSION, products);
        Path path = tempDir.resolve("catalog-v3.bin");
        BinarySerializer serializer = new BinarySerializer();
        serializer.save(catalogData, path);
        CatalogData loaded = serializer.load(path);
        CatalogMigrator.migrateToCurrent(loaded);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, loaded.getSchemaVersion());
        assertEquals(1, loaded.getProducts().length);

        assertEquals(1, loaded.getProducts()[0].getArticle());
        assertEquals("pillow", loaded.getProducts()[0].getName());
        assertEquals("house", loaded.getProducts()[0].getCategory());
        assertEquals(10, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
        assertEquals("Проверить упаковку", loaded.getProducts()[0].getNote());
        assertEquals(4, loaded.getProducts()[0].getRating());
    }

    @Test
    public void jsonCurrentVersionKeepsData() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        product.setNote("Проверить упаковку");
        product.setRating(4);
        Product[] products = {product};
        CatalogData catalogData = new CatalogData(CatalogData.CURRENT_SCHEMA_VERSION, products);
        Path path = tempDir.resolve("catalog-v3.json");
        JsonSerializer serializer = new JsonSerializer();
        serializer.save(catalogData, path);
        CatalogData loaded = serializer.load(path);
        CatalogMigrator.migrateToCurrent(loaded);
        assertEquals(CatalogData.CURRENT_SCHEMA_VERSION, loaded.getSchemaVersion());
        assertEquals(1, loaded.getProducts().length);

        assertEquals(1, loaded.getProducts()[0].getArticle());
        assertEquals("pillow", loaded.getProducts()[0].getName());
        assertEquals("house", loaded.getProducts()[0].getCategory());
        assertEquals(10, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
        assertEquals("Проверить упаковку", loaded.getProducts()[0].getNote());
        assertEquals(4, loaded.getProducts()[0].getRating());
    }

}
