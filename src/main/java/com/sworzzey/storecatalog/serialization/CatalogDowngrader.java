package com.sworzzey.storecatalog.serialization;


import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;

import java.io.IOException;

public class CatalogDowngrader {
    public static CatalogData downgrade(CatalogData source, int targetVersion) throws IOException {
        CatalogValidator.validate(source);
        if (targetVersion < CatalogData.MIN_SCHEMA_VERSION || targetVersion > source.getSchemaVersion()) {
            throw new IOException("Версия каталога может быть от " + CatalogData.MIN_SCHEMA_VERSION + " до " + source.getSchemaVersion());
        }
        Product[] products = new Product[source.getProducts().length];
        for (int i = 0; i < products.length; i++) {
            Product copy;
            Product original = source.getProducts()[i];
            if (original instanceof WarrantyProduct) {
                copy = new WarrantyProduct(original.getArticle(), original.getName(), original.getCategory(), original.getPrice(), original.getStock(), ((WarrantyProduct) original).getWarrantyMonths());
            } else if (original instanceof DiscontinuedProduct) {
                copy = new DiscontinuedProduct(original.getArticle(), original.getName(), original.getCategory(), original.getPrice(), original.getStock());
            } else {
                copy = new Product(original.getArticle(), original.getName(), original.getCategory(), original.getPrice(), original.getStock());
            }

            if (targetVersion >= 2) {
                copy.setNote(original.getNote());
            }

            if (targetVersion >= 3) {
                copy.setRating(original.getRating());
            }

            products[i] = copy;
        }

        return new CatalogData(targetVersion, products);
    }
}
