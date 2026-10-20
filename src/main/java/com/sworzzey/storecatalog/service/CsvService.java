package com.sworzzey.storecatalog.service;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import com.sworzzey.storecatalog.structure.ProductStore;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;

public class CsvService {
    private static final String TYPE_PRODUCT = "PRODUCT";
    private static final String TYPE_DISCONTINUED = "DISCONTINUED";
    private static final String TYPE_WARRANTY = "WARRANTY";

    private static final int STANDARD_FIELD_COUNT = 6;
    private static final int WARRANTY_FIELD_COUNT = 7;

    private static final int MAX_STORED_ERRORS = 100;
    private static final int PROGRESS_STEP = 100;


    private Product parseProduct(String[] values) {
        return switch (values[0]) {
            case TYPE_PRODUCT -> new Product(
                    Long.parseLong(values[1]),
                    values[2],
                    values[3],
                    Double.parseDouble(values[4]),
                    Integer.parseInt(values[5])
            );

            case TYPE_WARRANTY -> new WarrantyProduct(
                    Long.parseLong(values[1]),
                    values[2],
                    values[3],
                    Double.parseDouble(values[4]),
                    Integer.parseInt(values[5]),
                    Integer.parseInt(values[6])
            );

            case TYPE_DISCONTINUED -> new DiscontinuedProduct(
                    Long.parseLong(values[1]),
                    values[2],
                    values[3],
                    Double.parseDouble(values[4]),
                    Integer.parseInt(values[5])
            );

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип товара: " + values[0]
            );
        };
    }

    private boolean isKnownType(String type) {
        return TYPE_PRODUCT.equals(type)
                || TYPE_DISCONTINUED.equals(type)
                || TYPE_WARRANTY.equals(type);
    }

    private boolean hasCorrectFieldCount(String[] values) {
        return switch (values[0]) {
            case TYPE_PRODUCT, TYPE_DISCONTINUED ->
                    values.length == STANDARD_FIELD_COUNT;
            case TYPE_WARRANTY ->
                    values.length == WARRANTY_FIELD_COUNT;
            default -> false;
        };
    }

    public CsvLoadResult load(Path file, ProductStore target, BooleanSupplier cancelled, DoubleConsumer progress) throws IOException {
        long totalLines = 0;
        List<CsvException> errors = new ArrayList<>();
        int totalErrors = 0;
        int loaded = 0;
        int lineNumber = 0;
        try (BufferedReader br = Files.newBufferedReader(file)) {
            while (br.readLine() != null) {
                totalLines++;
            }
        }
        if (totalLines == 0) {
            CsvLoadResult result = new CsvLoadResult(0, 0, List.of(), true);
            progress.accept(1);
            return result;
        }

        try (BufferedReader br = Files.newBufferedReader(file)){
            String line;
            while ((line = br.readLine()) != null) {
                if (cancelled.getAsBoolean()) {
                    return new CsvLoadResult(loaded, totalErrors, errors, false);
                }
                lineNumber++;
                if (line.isBlank()) continue;
                String[] values = line.split(",", -1);
                if (!isKnownType(values[0])) {
                    totalErrors++;
                    if (errors.size() < MAX_STORED_ERRORS) {
                        errors.add(new CsvException(CsvException.CsvErrorCode.UNKNOWN_TYPE, lineNumber));
                    }
                } else if (!hasCorrectFieldCount(values)) {
                    totalErrors++;
                    if (errors.size() < MAX_STORED_ERRORS) {
                        errors.add(new CsvException(CsvException.CsvErrorCode.WRONG_FIELD_COUNT, lineNumber));
                    }
                } else {
                    try {
                        if(target.add(parseProduct(values))) {
                            loaded++;
                        } else {
                            totalErrors++;
                            if (errors.size() < MAX_STORED_ERRORS) {
                                errors.add(new CsvException(CsvException.CsvErrorCode.DUPLICATE_ARTICLE, lineNumber));
                            }
                        }
                    } catch (NumberFormatException e) {
                        totalErrors++;
                        if (errors.size() < MAX_STORED_ERRORS) {
                            errors.add(new CsvException(CsvException.CsvErrorCode.BAD_NUMBER, lineNumber));
                        }
                    }
                }
                if (lineNumber % PROGRESS_STEP == 0) {
                    progress.accept((double)lineNumber/totalLines);
                }
            }
        }
        progress.accept(1);
        return new CsvLoadResult(loaded, totalErrors, errors, true);
    }

    public void saveCsv(Path file, List<Product> products) throws IOException {
        List<String> csvLines = new ArrayList<>();

        for (Product p : products) {
            String line;
            if (p instanceof WarrantyProduct) {
                line = "WARRANTY," +
                        p.getArticle() + "," +
                        p.getName() + "," +
                        p.getCategory() + "," +
                        p.getPrice() + "," +
                        p.getStock() + "," +
                        ((WarrantyProduct) p).getWarrantyMonths();
            } else if (p instanceof DiscontinuedProduct) {
                line = "DISCONTINUED," +
                        p.getArticle() + "," +
                        p.getName() + "," +
                        p.getCategory() + "," +
                        p.getPrice() + "," +
                        p.getStock();
            } else {
                line = "PRODUCT," +
                        p.getArticle() + "," +
                        p.getName() + "," +
                        p.getCategory() + "," +
                        p.getPrice() + "," +
                        p.getStock();
            }
            csvLines.add(line);
        }

        Files.write(file, csvLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
