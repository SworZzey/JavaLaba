package com.sworzzey.storecatalog.service;

import com.sworzzey.storecatalog.structure.ProductStore;

public record LoadedCatalog(ProductStore store, CsvLoadResult result) {

}
