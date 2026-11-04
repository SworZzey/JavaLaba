package com.sworzzey.storecatalog.serialization;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sworzzey.storecatalog.serialization.CatalogValidator.validate;

public class BinarySerializer implements Serializer{
    @Override
    public String format() {
        return "bin";
    }

    @Override
    public void save(CatalogData data, Path path) throws IOException {
        validate(data);
        try (
                OutputStream fileStream = Files.newOutputStream(path);
                ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)
        ) {
            objectStream.writeObject(data);
        }
    }

    @Override
    public CatalogData load(Path path) throws IOException {
        try (
                InputStream fileStream = Files.newInputStream(path);
                ObjectInputStream objectStream = new ObjectInputStream(fileStream)
        ) {
            Object loaded = objectStream.readObject();
            if (loaded instanceof CatalogData catalog) {
                validate(catalog);
                return catalog;
            }
            throw new IOException("Объект неподходящего типа");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
