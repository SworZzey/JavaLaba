package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sworzzey.storecatalog.model.Product;
import java.io.Serializable;

public class CatalogData implements Serializable{
    @JsonProperty("schema_version")
    @JsonAlias("schemaVersion")
    private int schemaVersion;
    private Product[] products;
    private static final long serialVersionUID = 1L;
    public static final int MIN_SCHEMA_VERSION = 1;
    public static final int CURRENT_SCHEMA_VERSION = 3;

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
