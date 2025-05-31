module com.example.catcoins {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires java.mail;

    opens com.example.catcoins to javafx.fxml;
    exports com.example.catcoins;
    exports com.example.catcoins.model;
    opens com.example.catcoins.model to javafx.fxml;
}