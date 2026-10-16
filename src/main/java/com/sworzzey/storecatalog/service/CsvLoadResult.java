package com.sworzzey.storecatalog.service;

import java.util.List;

public class CsvLoadResult {
    private final int productsLoaded;
    private final int errors;
    private final List<CsvException> errList;
    private final boolean finished;

    public CsvLoadResult(int productsLoaded, int errors, List<CsvException> errList, boolean finished) {
        this.productsLoaded = productsLoaded;
        this.errors = errors;
        this.errList = List.copyOf(errList);
        this.finished = finished;
    }

    public int getLoaded() {
        return productsLoaded;
    }

    public int getErrors() {
        return errors;
    }

    public List<CsvException> getErrList() {
        return errList;
    }

    public boolean isFinished() {
        return finished;
    }
}
