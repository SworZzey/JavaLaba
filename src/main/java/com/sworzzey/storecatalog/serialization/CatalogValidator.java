package com.sworzzey.storecatalog.serialization;

import java.io.IOException;

public class CatalogValidator {
    public static void validate(CatalogData data) throws IOException{
        if (data == null) {
            throw new IOException("Каталог отсутствует");
        }
        int version = data.getSchemaVersion();
        if (version < CatalogData.MIN_SCHEMA_VERSION
                || version > CatalogData.CURRENT_SCHEMA_VERSION) {
            throw new IOException("Неподдерживаемая версия");
        }
        if (data.getProducts() == null) {
            throw new IOException("Массив товаров отсутствует");
        }
        for (int i = 0; i < data.getProducts().length; i++) {
            if (data.getProducts()[i] == null) {
                throw new IOException("Товар с индексом " + i + " отсутствует");
            }
            if (version >= 2) {
                if (data.getProducts()[i].getNote() == null) {
                    throw new IOException("Заметка товара с индексом " + i + " отсутствует");
                }
            }
            if (version >= 3) {
                if (data.getProducts()[i].getRating() > 5 || data.getProducts()[i].getRating() < 0) {
                    throw new IOException("Рейтинг товара с индексом " + i + " должен быть от 0 до 5");
                }
            }
        }
    }
}
