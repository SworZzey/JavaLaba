package com.sworzzey.storecatalog.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class JsonSerializer implements Serializer{
    private ObjectMapper convert = new ObjectMapper();

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void save(CatalogData data, Path path) throws IOException {
        convert.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
    }

    @Override
    public CatalogData load(Path path) throws IOException {
        return convert.readValue(path.toFile(), CatalogData.class);
    }

}

