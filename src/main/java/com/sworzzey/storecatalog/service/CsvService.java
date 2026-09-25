package com.sworzzey.storecatalog.service;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.nio.file.Files;

public class CsvService {
    private List<CsvException> lastErrors = new ArrayList<>();

    public List<CsvException> getLastErrors() {
        return lastErrors;
    }

    public List<Product> loadCsv(Path file) throws IOException {
        lastErrors.clear();
        List<Product> res = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                String[] values = line.split(",");

                if (!(values[0].equals("PRODUCT") || values[0].equals("DISCONTINUED") || values[0].equals("WARRANTY"))) {
                    lastErrors.add(new CsvException(CsvException.CsvErrorCode.UNKNOWN_TYPE, lineNumber));
                } else if (((values[0].equals("PRODUCT") || values[0].equals("DISCONTINUED")) && values.length != 6) || (values[0].equals("WARRANTY") && values.length != 7)) {
                    lastErrors.add(new CsvException(CsvException.CsvErrorCode.WRONG_FIELD_COUNT, lineNumber));
                } else {
                    try {
                        switch (values[0]) {
                            case "PRODUCT" ->
                                    res.add(new Product(Long.parseLong(values[1]), values[2], values[3], Double.parseDouble(values[4]), Integer.parseInt(values[5])));
                            case "WARRANTY" ->
                                    res.add(new WarrantyProduct(Long.parseLong(values[1]), values[2], values[3], Double.parseDouble(values[4]), Integer.parseInt(values[5]), Integer.parseInt(values[6])));
                            case "DISCONTINUED" ->
                                    res.add(new DiscontinuedProduct(Long.parseLong(values[1]), values[2], values[3], Double.parseDouble(values[4]), Integer.parseInt(values[5])));
                        }
                    } catch (NumberFormatException e) {
                        lastErrors.add(new CsvException(CsvException.CsvErrorCode.BAD_NUMBER, lineNumber));
                    }
                }
            }
        }
        return res;
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
