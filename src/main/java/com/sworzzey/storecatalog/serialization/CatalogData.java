package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sworzzey.storecatalog.model.Product;

import java.io.*;

public class CatalogData implements Serializable{
    private int schema_version;
    private Product[] products;
    private static final long serialVersionUID = 1L;
    public static final int MIN_SCHEMA_VERSION = 1;
    public static final int CURRENT_SCHEMA_VERSION = 3;

    public CatalogData(int version, Product[] products) {
        this.schema_version = version;
        this.products = products;
    }

    public CatalogData() {}
    @JsonProperty("schema_version")
    public int getSchemaVersion() {
        return schema_version;
    }

    public Product[] getProducts() {
        return products;
    }
    @JsonAlias("schemaVersion")
    @JsonProperty("schema_version")
    public void setSchemaVersion(int schemaVersion) {
        this.schema_version = schemaVersion;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField putField = out.putFields();
        putField.put("schema_version", schema_version);
        putField.put("products", products);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        ObjectStreamClass descriptor = fields.getObjectStreamClass();
        ObjectStreamField versionField = descriptor.getField("schema_version");
        if (versionField == null) {
            versionField = descriptor.getField("schemaVersion");
            if (versionField == null) {
                throw new IOException("Поле версии отсутствует");
            }
        }
        if (versionField.getType() != int.class) {
            throw new IOException("Неверный тип");
        }
        schema_version = fields.get(versionField.getName(), 0);
        Object prods = fields.get("products", null);
        if (!(prods instanceof Product[])) {
            throw new IOException("Массив с продуктами некорректен");
        }
        products = (Product[])prods;
        CatalogValidator.validate(this);
    }
}
