package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.service.*;
import com.sworzzey.storecatalog.structure.ProductStore;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.sworzzey.storecatalog.structure.BTreeProductStore;

public class HelloController {
    //массив с товарами
    private final ObservableList<Product> products = FXCollections.observableArrayList();
    private final CatalogService storage = new CatalogService(new BTreeProductStore());



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

    @FXML
    private Button btnDelete;

    @FXML
    private TextField productAmount;

    @FXML
    private Button btnGenerate;

    @FXML
    private Button btnStop;

    @FXML
    private ProgressBar progressBar;


    //клик на кнопку добавить
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
            if (!storage.add(newProduct)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Не удалось добавить товар");
                alert.setContentText("Товар с таким артикулом уже существует.");
                alert.showAndWait();
            }

            refreshProducts();
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
            if(!storage.replace(selectedProduct.getArticle(), changedProduct)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Не удалось изменить товар");
                alert.setContentText("Товар с таким артикулом уже существует.");
                alert.showAndWait();
            }
            refreshProducts();
        }
    }

    //кнопка удалить
    @FXML
    private void onDeleteClicked() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            return;
        }
        if (!storage.remove(selectedProduct.getArticle())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось удалить товар");
            alert.setContentText("Произошла ошибка при удалении товара");
            alert.showAndWait();
        }
        refreshProducts();
    }


    private void finishBackgroundTask() {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);

        generateTask = null;
        loadTask = null;

        setButtons(false);
    }


    //загрузить из csv
    @FXML
    private void onAddCsvClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите csv файл");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );

        Stage stage = (Stage) btnLoadCsv.getScene().getWindow();

        File selevtedFile = fileChooser.showOpenDialog(stage);

        if (selevtedFile == null) {
            return;
        }

        Path pathToFile = selevtedFile.toPath();
        loadTask = new CsvLoadTask(pathToFile);
        progressBar.progressProperty().bind(loadTask.progressProperty());

        loadTask.setOnSucceeded(event -> {
            LoadedCatalog newStore = loadTask.getValue();
            if (newStore == null) {
                finishBackgroundTask();
                return;
            }

            storage.replaceStorage(newStore.store());
            refreshProducts();

            CsvLoadResult result = newStore.result();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Загрузка CSV");
            alert.setHeaderText("Загрузка завершена");
            alert.setContentText(
                    "Загружено товаров: " + result.getLoaded() +
                            "\nКоличество ошибок: " + result.getErrors()
            );
            alert.showAndWait();

            if (!result.getErrList().isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();

                for (CsvException error : result.getErrList()) {
                    errorMessage.append("Строка ")
                            .append(error.getLineNumber())
                            .append(": ")
                            .append(error.getErrorCode())
                            .append("\n");
                }

                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                errorAlert.setTitle("Ошибки CSV");
                errorAlert.setHeaderText("При загрузке обнаружены ошибки");

                TextArea textArea = new TextArea(errorMessage.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                textArea.setPrefRowCount(15);

                errorAlert.getDialogPane().setContent(textArea);
                errorAlert.getDialogPane().setPrefWidth(550);
                errorAlert.getDialogPane().setPrefHeight(400);

                errorAlert.showAndWait();
            }

            finishBackgroundTask();
        });

        loadTask.setOnCancelled(event -> {
            finishBackgroundTask();
        });

        loadTask.setOnFailed(event -> {
            Throwable er = loadTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка загрузки товаров " + er);
            alert.showAndWait();
            finishBackgroundTask();
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        setButtons(true);
        thread.start();
    }

    private final CsvService csvService = new CsvService();
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
            csvService.saveCsv(whereSave, storage.getAllSorted());

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка сохранения");
            alert.setContentText("Ошибка сохранения csv файла");
            alert.showAndWait();
        }
    }


    //сгенерировать товары
    private GenerateCatalogTask generateTask;
    private CsvLoadTask loadTask;

    private static final int MAX_GENERATE_COUNT = 1_000_000;

    private void updateSelectionButtons() {
        Product selected = productTable.getSelectionModel().getSelectedItem();

        boolean hasSelection = selected != null;

        btnDelete.setDisable(!hasSelection);
        btnEdit.setDisable(!hasSelection || selected instanceof DiscontinuedProduct);
    }

    //выключение кнопок
    private void setButtons(boolean generating) {
        btnAdd.setDisable(generating);
        btnLoadCsv.setDisable(generating);
        btnSaveCsv.setDisable(generating);
        btnGenerate.setDisable(generating);
        productAmount.setDisable(generating);
        btnStop.setDisable(!generating);
        productTable.setDisable(generating);

        // При генерации эти кнопки всегда отключены.
        if (generating) {
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
        } else {
            updateSelectionButtons();
        }
    }

    //кнопка отмена
    @FXML
    public void onStopClicked() {
        if (generateTask != null) {
            generateTask.cancel();
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
    }


    //кнопка сгенерировать
    @FXML
    public void onGenerateClicked() {
        int amount;
        try {
            amount = Integer.parseInt(productAmount.getText().trim());
            if (amount < 1 || amount > MAX_GENERATE_COUNT) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Укажите нормально число");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Укажите нормально число");
            alert.showAndWait();
            return;
        }
        if (storage.size() > 0) {
            Alert option = new Alert(Alert.AlertType.CONFIRMATION);
            option.setTitle("Подтвердите действие");
            option.setHeaderText("Текущий каталог будет заменен другими данными");
            Optional<ButtonType> result = option.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        generateTask = new GenerateCatalogTask(amount, System.currentTimeMillis(), 1);
        //привязываем прогресс бар
        progressBar.progressProperty().bind(generateTask.progressProperty());

        generateTask.setOnSucceeded(event -> {
            ProductStore newStore = generateTask.getValue();

            storage.replaceStorage(newStore);
            refreshProducts();

            finishBackgroundTask();
        });

        generateTask.setOnCancelled(event -> {
            finishBackgroundTask();
        });

        generateTask.setOnFailed(event -> {
            Throwable er = generateTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка генерации товаров " + er);
            alert.showAndWait();
            finishBackgroundTask();
        });

        Thread thread = new Thread(generateTask);
        thread.setDaemon(true);
        setButtons(true);
        thread.start();
    }



    private void refreshProducts() {
        products.setAll(storage.getAllSorted());
    }

    @FXML
    public void initialize() {
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

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

        productTable.setItems(products);

        //изменение состояния кнопки редактирования
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldProduct, newProduct) -> {
                    if (generateTask == null) {
                        updateSelectionButtons();
                    }
                }
        );
    }
}
