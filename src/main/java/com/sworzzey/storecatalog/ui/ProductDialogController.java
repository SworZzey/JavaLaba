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
    private TextField txtNote;

    @FXML
    private TextField txtRating;

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
        txtNote.setText(product.getNote());
        txtRating.setText(String.valueOf(product.getRating()));
    }

    @FXML
    private void onOkClicked(){
        try {
            Product product;
            if (!txtWarranty.getText().isBlank() && !txtWarranty.getText().equals("0")) {
                product = new WarrantyProduct(Long.parseLong(txtArticle.getText()), txtName.getText(), txtCategory.getText(), Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()), Integer.parseInt(txtWarranty.getText()));
                product.setNote(txtNote.getText());
                product.setRating(Integer.parseInt(txtRating.getText()));
            } else {
                product = new Product(Long.parseLong(txtArticle.getText()), txtName.getText(), txtCategory.getText(), Double.parseDouble(txtPrice.getText()), Integer.parseInt(txtStock.getText()));
                product.setNote(txtNote.getText());
                product.setRating(Integer.parseInt(txtRating.getText()));
            }

            Stage stage = (Stage) btnOk.getScene().getWindow();
            result = product;
            stage.close();
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Некорректные данные");
            alert.setContentText("Ошибка: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}
