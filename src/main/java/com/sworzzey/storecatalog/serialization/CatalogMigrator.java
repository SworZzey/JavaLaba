package com.sworzzey.storecatalog.serialization;

import java.io.IOException;

public class CatalogMigrator {

    private static void migrateV1ToV2(CatalogData catalog) throws IOException {
        CatalogValidator.validate(catalog);
        if (catalog.getSchemaVersion() != 1) {
            throw new IOException("Версия схемы не равна 1");
        }
        for (int i = 0; i < catalog.getProducts().length; i++) {
            catalog.getProducts()[i].setNote("");
        }
        catalog.setSchemaVersion(2);
    }


    private static void migrateV2ToV3(CatalogData catalog) throws IOException {
        CatalogValidator.validate(catalog);
        if (catalog.getSchemaVersion() != 2) {
            throw new IOException("Версия схемы не равна 2");
        }
        for (int i = 0; i < catalog.getProducts().length; i++) {
            catalog.getProducts()[i].setRating(0);
        }
        catalog.setSchemaVersion(3);
    }


    public static void migrateToCurrent(CatalogData catalog) throws IOException {
        CatalogValidator.validate(catalog);
        while (catalog.getSchemaVersion() < CatalogData.CURRENT_SCHEMA_VERSION) {
            switch (catalog.getSchemaVersion()) {
                case 1:
                    migrateV1ToV2(catalog);
                    break;
                case 2:
                    migrateV2ToV3(catalog);
                    break;
                default:
                    throw new IOException("Нет миграции для версии " + catalog.getSchemaVersion());
            }
        }
    }



}
