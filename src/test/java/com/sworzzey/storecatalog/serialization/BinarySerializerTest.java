package com.sworzzey.storecatalog.serialization;

import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class BinarySerializerTest {
    @TempDir
    private Path tempDir;

    //позитивные тесты
    @Test
    public void roundTripPreservesProduct() throws IOException {
        Product product = new Product(1, "pillow", "house", 10, 2);
        Product[] products = {product};
        CatalogData ser = new CatalogData(1, products);

        Path path = tempDir.resolve("catalog.bin");
        BinarySerializer serializer = new BinarySerializer();
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


        Path path = tempDir.resolve("catalog.bin");
        BinarySerializer serializer = new BinarySerializer();
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

        Path path = tempDir.resolve("catalog.bin");
        BinarySerializer serializer = new BinarySerializer();

        serializer.save(ser, path);
        CatalogData loaded = serializer.load(path);

        assertEquals(1, loaded.getSchemaVersion());
        assertEquals(0, loaded.getProducts().length);
    }

    //негативные тесты, не бинарный файл
    @Test
    public void rejectsCorruptedFile() throws IOException {
        Path path = tempDir.resolve("broken.bin");
        Files.writeString(path, "Не бинарный файл");

        BinarySerializer serializer = new BinarySerializer();
        assertThrows(IOException.class, () -> serializer.load(path));
    }


    //неверный тип
    @Test
    public void rejectsWrongObjectType() throws IOException {
        Path path = tempDir.resolve("wrong-type.bin");
        try (
            OutputStream fileStream = Files.newOutputStream(path);
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)
        ) {
            objectStream.writeObject("Строка вместо каталога");
        }
        BinarySerializer serializer = new BinarySerializer();
        assertThrows(IOException.class, () -> serializer.load(path));
    }

    //некорректная версия
    @Test
    public void rejectsUnsupportedVersion() throws IOException {
        Product[] products = new Product[0];
        CatalogData data = new CatalogData(CatalogData.CURRENT_SCHEMA_VERSION+1, products);

        Path path = tempDir.resolve("unsupported-version.bin");
        try (
                OutputStream fileStream = Files.newOutputStream(path);
                ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)
        ) {
            objectStream.writeObject(data);
        }
        BinarySerializer serializer = new BinarySerializer();
        IOException error = assertThrows(IOException.class, () -> serializer.load(path));
        assertEquals("Неподдерживаемая версия", error.getMessage());
    }
}
