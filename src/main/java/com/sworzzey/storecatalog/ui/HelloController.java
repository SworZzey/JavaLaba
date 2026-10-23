package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.service.*;
import com.sworzzey.storecatalog.structure.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    @FXML
    private Canvas treeCanvas;

    @FXML
    private Button btnStats;

    @FXML
    private Button btnFind;

    private final TreeRenderer treeRenderer = new TreeRenderer();

    private static final int MAX_DRAWN_NODES = 64;
    private static final int TRUNCATED_LEVELS = 3;
    private static final double MAX_CANVAS_WIDTH = 8000;
    private static final double MIN_CANVAS_WIDTH = 300;


    //Алерт с результатом статистики
    private void showTreeStatsAlert() {
        long start = System.nanoTime();
        NodeFillStats stats = storage.getFillStats();
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000.0;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Статистика B-дерева");
        alert.setHeaderText("Характеристики структуры дерева");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        addStatRow(grid, 0, "Количество узлов:",
                String.valueOf(stats.getNodeCount()));

        addStatRow(grid, 1, "Количество листьев:",
                String.valueOf(stats.getLeafCount()));

        addStatRow(grid, 2, "Высота дерева:",
                String.valueOf(stats.getHeight()));

        addStatRow(grid, 3, "Средняя заполненность:",
                String.format("%.2f%%", stats.getAverageFillPercent()));

        addStatRow(grid, 4, "Время расчёта статистики:",
                String.format("%.4f мс", time));

        VBox content = new VBox(15);
        content.setPadding(new Insets(5));
        content.getChildren().add(grid);

        Label distributionTitle = new Label("Распределение узлов по количеству ключей");
        distributionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        GridPane distributionGrid = new GridPane();
        distributionGrid.setHgap(20);
        distributionGrid.setVgap(8);

        int[] distribution = stats.getDistribution();

        for (int i = 1; i < distribution.length; i++) {
            distributionGrid.addRow(
                    i - 1,
                    new Label(i + " ключ."),
                    new Label(String.valueOf(distribution[i]) + " узл.")
            );
        }

        content.getChildren().addAll(distributionTitle, distributionGrid);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setMinWidth(380);
        alert.showAndWait();
    }

    private void addStatRow(
            GridPane grid,
            int row,
            String title,
            String value
    ) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label valueLabel = new Label(value);

        grid.addRow(row, titleLabel, valueLabel);
    }


    //Алерт с результатом поиска
    private void showProductAlert(Product product, double time) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Информация о товаре");
        alert.setHeaderText(product.getName());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 5, 5, 5));

        grid.addRow(0, new Label("Артикул:"), new Label(String.valueOf(product.getArticle())));
        grid.addRow(1, new Label("Название:"), new Label(product.getName()));
        grid.addRow(2, new Label("Категория:"), new Label(product.getCategory()));
        grid.addRow(3, new Label("Цена:"), new Label(String.format("%.2f ₽", product.getPrice())));
        grid.addRow(4, new Label("Количество на складе:"), new Label(String.valueOf(product.getStock())));
        if (product instanceof WarrantyProduct warrantyProduct) {
            grid.addRow(
                    5,
                    new Label("Гарантия:"),
                    new Label(warrantyProduct.getWarrantyMonths() + " мес.")
            );
        }
        grid.addRow(
                product instanceof WarrantyProduct ? 6 : 5,
                new Label("Время выполнения:"),
                new Label(String.format("%.4f мс", time))
        );

        // Выделяем названия полей жирным
        for (int i = 0; i < grid.getChildren().size(); i += 2) {
            if (grid.getChildren().get(i) instanceof Label label) {
                label.setStyle("-fx-font-weight: bold;");
            }
        }

        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setMinWidth(350);
        alert.showAndWait();
    }

    //кнопка найти
    @FXML
    private void onFindClicked() throws IOException{
        VisitTrace trace = new VisitTrace();
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Поиск товара");
        dialog.setHeaderText("Найти товар по артикулу");
        dialog.setContentText("Введите артикул:");

        dialog.showAndWait().ifPresent(input -> {
            String article = input.trim();

            if (article.isEmpty()) {
                return;
            }
            long start = System.nanoTime();
            Product found = storage.find(Long.parseLong(article), trace);
            long end = System.nanoTime();
            double time = (end - start) / 1_000_000.0;
            if (found != null) {
                redrawTree(trace);
                showProductAlert(found, time);
            } else {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Товар с таким артикулом не найден."
                ).showAndWait();
            }
        });
    }


    //кнопка статистика
    @FXML
    private void onStatsClicked() throws IOException {
        showTreeStatsAlert();
    }



    //клик на кнопку добавить
    @FXML
    private void onAddClicked() throws IOException {
        VisitTrace trace = new VisitTrace();
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
            long start = System.nanoTime();
            boolean added = storage.add(newProduct, trace);
            long end = System.nanoTime();
            double time = (end - start) / 1_000_000.0;
            if (!added) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Предупреждение");
                alert.setHeaderText("Не удалось добавить товар");
                alert.setContentText("Товар с таким артикулом уже существует.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Продукт добавлен");
                alert.setHeaderText(null);
                alert.setContentText(String.format("Время добавления: %.4f мс", time));
                alert.showAndWait();
            }

            refreshProducts();
            redrawTree(trace);
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
            redrawTree();
        }
    }

    //кнопка удалить
    @FXML
    private void onDeleteClicked() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            return;
        }
        long start = System.nanoTime();
        boolean removed = storage.remove(selectedProduct.getArticle());
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000.0;
        if (!removed) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось удалить товар");
            alert.setContentText("Произошла ошибка при удалении товара");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Продукт удалён");
            alert.setHeaderText(null);
            alert.setContentText(String.format("Время удаления: %.4f мс", time));
            alert.showAndWait();
        }
        refreshProducts();
        redrawTree();
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
            redrawTree();

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
            redrawTree();

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


    private void redrawTree() {
        redrawTree(null);
    }

    private void redrawTree(VisitTrace trace) {
        NodeFillStats stats = storage.getFillStats();

        int nodeCount = stats.getNodeCount();
        int totalLevels = stats.getHeight() + 1;

        boolean showFullTree = nodeCount <= MAX_DRAWN_NODES;
        int levelsToShow = showFullTree
                ? Math.max(1, totalLevels)
                : TRUNCATED_LEVELS;

        NodeView view = storage.getSnapshot(levelsToShow);
        TreeRenderer.TreeDimensions size = treeRenderer.calculateSize(view);

        if (showFullTree && size.width > MAX_CANVAS_WIDTH) {
            showFullTree = false;
            levelsToShow = TRUNCATED_LEVELS;
            view = storage.getSnapshot(levelsToShow);
            size = treeRenderer.calculateSize(view);
        }

        treeCanvas.setWidth(Math.max(MIN_CANVAS_WIDTH, size.width));
        treeCanvas.setHeight(Math.max(100, size.height));

        treeRenderer.draw(treeCanvas.getGraphicsContext2D(), view, trace);
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
