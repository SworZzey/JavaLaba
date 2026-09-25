module com.sworzzey.storecatalog {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.sworzzey.storecatalog to javafx.fxml;
    exports com.sworzzey.storecatalog;
    exports com.sworzzey.storecatalog.ui;
    opens com.sworzzey.storecatalog.ui to javafx.fxml;
    opens com.sworzzey.storecatalog.model to javafx.base;
}