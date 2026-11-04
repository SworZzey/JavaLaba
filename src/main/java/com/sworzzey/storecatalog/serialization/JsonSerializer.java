package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        convert.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
    }

    @Override
    public CatalogData load(Path path) throws IOException {
        CatalogData data = convert.readValue(path.toFile(), CatalogData.class);
        validate(data);
        return data;
    }

}

