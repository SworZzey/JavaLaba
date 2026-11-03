package com.sworzzey.storecatalog.serialization;

import java.io.IOException;
import java.nio.file.Path;

public interface Serializer {

    String format();

    void save(CatalogData data, Path path) throws IOException;

    CatalogData load(Path path) throws IOException;
}
