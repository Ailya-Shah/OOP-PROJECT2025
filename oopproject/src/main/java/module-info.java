module com.example.oopproject {
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
    requires jakarta.mail;
    requires java.net.http;
    requires mysql.connector.j;

    opens com.example.oopproject to javafx.fxml;
    exports com.example.oopproject;
}