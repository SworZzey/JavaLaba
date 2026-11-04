package com.sworzzey.storecatalog.serialization;

import java.io.IOException;

public class CatalogValidator {
    public static void validate(CatalogData data) throws IOException{
        if (data == null) {
            throw new IOException("Каталог отсутствует");
        }
        if (data.getSchemaVersion() != CatalogData.CURRENT_SCHEMA_VERSION) {
            throw new IOException("Неподдерживаемая версия");
        }
        if (data.getProducts() == null) {
            throw new IOException("Массив товаров отсутствует");
        }
    }
}
