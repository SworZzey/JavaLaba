package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.WarrantyProduct;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class ProductDialogController {

    @FXML
    private TextField txtArticle;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtCategory;

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtWarranty;

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;



    private Product result;

    public Product getProduct() {
        return result;
    }
    public void setProduct(Product product) {
        txtArticle.setText(String.valueOf(product.getArticle()));
        txtName.setText(product.getName());
        txtCategory.setText(product.getCategory());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtStock.setText(String.valueOf(product.getStock()));
        if (product instanceof WarrantyProduct) {
            txtWarranty.setText(String.valueOf(((WarrantyProduct) product).getWarrantyMonths()));
        } else {
            txtWarranty.clear();
        }
    }

    @FXML
    private void onOkClicked(){
        try {
            if (!txtWarranty.getText().isBlank() && !txtWarranty.getText().equals("0")) {
                result = new WarrantyProduct(Long.parseLong(txtArticle.getText()), txtName.getText(), txtCategory.getText(), Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()), Integer.parseInt(txtWarranty.getText()));
            } else {
                result = new Product(Long.parseLong(txtArticle.getText()), txtName.getText(), txtCategory.getText(), Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()));
            }

            Stage stage = (Stage) btnOk.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Некорректные данные");
            alert.setContentText("Проверьте числовые поля.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}
