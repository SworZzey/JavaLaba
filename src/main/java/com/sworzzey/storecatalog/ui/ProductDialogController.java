package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ProductDialogController {

    private Product result;

    public Product getProduct() {
        return result;
    }

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

}
