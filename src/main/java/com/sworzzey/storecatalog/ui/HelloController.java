package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.service.CsvException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import com.sworzzey.storecatalog.service.CsvService;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class HelloController {
    //массив с товарами
    private final ObservableList<Product> products = FXCollections.observableArrayList();


    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> typeColumn;

    @FXML
    private TableColumn<Product, Long> articleColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, Integer> warrantyColumn;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnLoadCsv;

    @FXML
    private Button btnSaveCsv;

    //клика на кнопку добавить
    @FXML
    private void onAddClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/sworzzey/storecatalog/product-dialog.fxml")
        );

        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Добавление товара");
        stage.setScene(scene);
        stage.showAndWait();

        ProductDialogController controller = loader.getController();
        Product newProduct = controller.getProduct();
        if (newProduct != null) {
            products.add(newProduct);
        }
    }

    //кнопка редактировать
    @FXML
    private void onEditClicked() throws IOException {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/sworzzey/storecatalog/product-dialog.fxml")
        );

        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setTitle("Редактирование товара");
        stage.setScene(scene);

        ProductDialogController controller = loader.getController();
        controller.setProduct(selectedProduct);
        stage.showAndWait();

        Product changedProduct = controller.getProduct();
        if (changedProduct != null) {
            products.set(products.indexOf(selectedProduct), changedProduct);
        }
    }

    //загрузить из csv
    private final CsvService csvService = new CsvService();
    @FXML
    private void onAddCsvClicked() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите csv файл");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
            );

            Stage stage = (Stage) btnLoadCsv.getScene().getWindow();

            File selevtedFile = fileChooser.showOpenDialog(stage);

            if(selevtedFile == null) {
                return;
            }

            Path pathToFile = selevtedFile.toPath();
            List<Product> productsFromCsv = csvService.loadCsv(pathToFile);
            products.clear();
            products.addAll(productsFromCsv);
            //кастомные ошибки
            if (!csvService.getLastErrors().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("При загрузке были найдены некоректные данные");

                StringBuilder sb = new StringBuilder();

                for (CsvException e : csvService.getLastErrors()) {
                    sb.append("Строка ")
                            .append(e.getLineNumber())
                            .append(": ")
                            .append(e.getErrorCode())
                            .append("\n");
                }

                alert.setContentText(sb.toString());
                alert.showAndWait();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка загрузки");
            alert.setContentText("Ошибка загрузки csv файла");
            alert.showAndWait();
        }
    }

    //Сохранить в CSV
    public void onSaveCsvClicked() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить в CSV");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
            );

            Stage stage = (Stage) btnSaveCsv.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if(file == null) {
                return;
            }

            Path whereSave = file.toPath();
            csvService.saveCsv(whereSave, products);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка сохранения");
            alert.setContentText("Ошибка сохранения csv файла");
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
        btnEdit.setDisable(true);

        //что лежит в колонках
        typeColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();

            if (product instanceof WarrantyProduct) {
                return new SimpleStringProperty("Warranty");
            } else if (product instanceof DiscontinuedProduct) {
                return new SimpleStringProperty("Discontinued");
            } else {
                return new SimpleStringProperty("Product");
            }
        });
        articleColumn.setCellValueFactory(new PropertyValueFactory<>("article"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        warrantyColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();

            if (product instanceof WarrantyProduct wp) {
                return new SimpleIntegerProperty(wp.getWarrantyMonths()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();
        });

        //тестовый товары
        products.addAll(
                new Product(123, "Pillow", "Clothes", 12334, 12),
                new WarrantyProduct(333, "Pot", "Kitchen", 1200, 10, 12),
                new DiscontinuedProduct(132, "Cup", "Kitchen", 1322, 0)
        );

        productTable.setItems(products);

        //изменение состояни кнопки редактирования
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldProduct, newProduct) -> {
                    if (newProduct == null) {
                        btnEdit.setDisable(true);
                        return;
                    }

                    btnEdit.setDisable(!newProduct.isEditable());
                }
        );
    }
}
