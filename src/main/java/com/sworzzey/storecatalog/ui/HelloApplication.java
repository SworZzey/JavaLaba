package com.sworzzey.storecatalog.ui;

import com.sworzzey.storecatalog.model.Product;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.sworzzey.storecatalog.model.Product;
import com.sworzzey.storecatalog.model.DiscontinuedProduct;
import com.sworzzey.storecatalog.model.WarrantyProduct;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/sworzzey/storecatalog/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setTitle("Catalog");
        stage.setScene(scene);
        stage.show();
    }
}
