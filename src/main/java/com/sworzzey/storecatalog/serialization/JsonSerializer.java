package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Path;

import static com.sworzzey.storecatalog.serialization.CatalogValidator.validate;

public class JsonSerializer implements Serializer{
    private ObjectMapper convert = new ObjectMapper();

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(CatalogData data, Path path) throws IOException {
        validate(data);
        ObjectNode tree = convert.valueToTree(data);
        JsonNode productsNode = tree.get("products");
        ArrayNode products = (ArrayNode) productsNode;
        for (int i = 0; i < products.size(); i++) {
            ObjectNode product = (ObjectNode) products.get(i);
            if (data.getSchemaVersion() <= 2) {
                product.remove("rating");
            }
            if(data.getSchemaVersion() <= 1) {
                product.remove("note");
            }
        }
        convert.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), tree);
    }

    @Override
    public CatalogData load(Path path) throws IOException {
        CatalogData data = convert.readValue(path.toFile(), CatalogData.class);
        validate(data);
        return data;
    }

}

