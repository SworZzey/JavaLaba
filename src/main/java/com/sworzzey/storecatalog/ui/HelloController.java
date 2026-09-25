package com.sworzzey.storecatalog.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;

import java.util.PrimitiveIterator;

public class HelloController {
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

    //обработчик клика на кнопку добавить
    @FXML
    private void onAddClicked() {
        products.add(
                new Product(999, "Test", "Test", 100, 5)
        );
    }

    private final ObservableList<Product> products = FXCollections.observableArrayList();

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
