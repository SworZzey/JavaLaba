package com.sworzzey.storecatalog.serialization;

import com.sworzzey.storecatalog.model.Product;
import java.io.Serializable;

public class CatalogData implements Serializable{
    private int schemaVersion;
    private Product[] products;
    private static final long serialVersionUID = 1L;
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public CatalogData(int version, Product[] products) {
        this.schemaVersion = version;
        this.products = products;
    }

    public CatalogData() {}

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public Product[] getProducts() {
        return products;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }
}
