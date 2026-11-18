package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonSerializerTest {
    @TempDir
    private Path tempDir;

    //позитивные тесты
    @Test
    public void roundTripPreservesProduct() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        Product[] products = {product};
        CatalogData ser = new CatalogData(1, products);

        Path path = tempDir.resolve("catalog.json");
        JsonSerializer serializer = new JsonSerializer();
        serializer.save(ser, path);
        CatalogData loaded = serializer.load(path);
        assertEquals(1, loaded.getSchemaVersion());
        assertEquals(1, loaded.getProducts().length);
        assertEquals(1, loaded.getProducts()[0].getArticle());
        assertEquals("pillow", loaded.getProducts()[0].getName());
        assertEquals("house", loaded.getProducts()[0].getCategory());
        assertEquals(10, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
    }

    @Test
    public void roundTripPreservesProductTypes() throws IOException {
        Product product = new Product(0, "mouse", "electronics", 10000, 2);
        WarrantyProduct warrantyProduct = new WarrantyProduct(1, "pillow", "house", 1233, 40, 24);
        DiscontinuedProduct discontinuedProduct = new DiscontinuedProduct(2, "monitor", "computer", 20000, 0);
        Product[] products = {product, warrantyProduct, discontinuedProduct};
        CatalogData ser = new CatalogData(1, products);


        Path path = tempDir.resolve("catalog.json");
        JsonSerializer serializer = new JsonSerializer();
        serializer.save(ser, path);
        CatalogData loaded = serializer.load(path);
        assertEquals(1, loaded.getSchemaVersion());
        assertEquals(3, loaded.getProducts().length);

        assertEquals(0, loaded.getProducts()[0].getArticle());
        assertEquals("mouse", loaded.getProducts()[0].getName());
        assertEquals("electronics", loaded.getProducts()[0].getCategory());
        assertEquals(10000, loaded.getProducts()[0].getPrice());
        assertEquals(2, loaded.getProducts()[0].getStock());
        assertEquals(Product.class, loaded.getProducts()[0].getClass());

        WarrantyProduct warranty = assertInstanceOf(WarrantyProduct.class, loaded.getProducts()[1]);
        assertEquals(1, loaded.getProducts()[1].getArticle());
        assertEquals("pillow", loaded.getProducts()[1].getName());
        assertEquals("house", loaded.getProducts()[1].getCategory());
        assertEquals(1233, loaded.getProducts()[1].getPrice());
        assertEquals(40, loaded.getProducts()[1].getStock());
        assertEquals(24, warranty.getWarrantyMonths());

        assertEquals(2, loaded.getProducts()[2].getArticle());
        assertEquals("monitor", loaded.getProducts()[2].getName());
        assertEquals("computer", loaded.getProducts()[2].getCategory());
        assertEquals(20000, loaded.getProducts()[2].getPrice());
        assertEquals(0, loaded.getProducts()[2].getStock());
        assertFalse(loaded.getProducts()[2].isEditable());
        assertEquals(DiscontinuedProduct.class, loaded.getProducts()[2].getClass());
    }

    @Test
    public void roundTripPreservesEmptyCatalog() throws IOException {
        Product[] products = new Product[0];
        CatalogData ser = new CatalogData(1, products);

        Path path = tempDir.resolve("catalog.json");
        JsonSerializer serializer = new JsonSerializer();

        serializer.save(ser, path);
        CatalogData loaded = serializer.load(path);

        assertEquals(1, loaded.getSchemaVersion());
        assertEquals(0, loaded.getProducts().length);
    }

    //негативные тесты, не json файл
    @Test
    public void rejectsCorruptedFile() throws IOException {
        Path path = tempDir.resolve("broken.json");
        Files.writeString(path, "Не json файл");

        JsonSerializer serializer = new JsonSerializer();
        assertThrows(IOException.class, () -> serializer.load(path));
    }

    //неподдерживаемая версия
    @Test
    public void rejectsUnsupportedVersion() throws IOException {
        Path path = tempDir.resolve("badVersion.json");
        try (InputStream input = getClass().getResourceAsStream("/serialization/unsupported-version.json")) {
            assertNotNull(input, "Не найден файл");
            Files.copy(input, path);
        }
        JsonSerializer serializer = new JsonSerializer();
        IOException error = assertThrows(IOException.class, () -> serializer.load(path));
        assertEquals("Неподдерживаемая версия", error.getMessage());
    }

    //неизвестный тип продукта
    @Test
    public void rejectsUnknownProductType() throws IOException {
        Path path = tempDir.resolve("badType.json");
        try (InputStream input = getClass().getResourceAsStream("/serialization/unknown-product-type.json")) {
            assertNotNull(input, "Не найден файл");
            Files.copy(input, path);
        }
        JsonSerializer serializer = new JsonSerializer();
        assertThrows(InvalidTypeIdException.class, () -> serializer.load(path));
    }

    //отсутствует массив товаров
    @Test
    public void rejectsMissingProducts() throws IOException {
        Path path = tempDir.resolve("noProducts.json");
        try (InputStream input = getClass().getResourceAsStream("/serialization/missing-products.json")) {
            assertNotNull(input, "Не найден файл");
            Files.copy(input, path);
        }
        JsonSerializer serializer = new JsonSerializer();
        IOException error = assertThrows(IOException.class, () -> serializer.load(path));
        assertEquals("Массив товаров отсутствует", error.getMessage());
    }
 }
