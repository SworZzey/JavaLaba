package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.structure.ProductStore;

public record CatalogImportResult(ProductStore store, int sourceVersion) {
}
