module github.hitman20081.todomanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires ch.qos.logback.classic; // Correct Logback module
    requires ch.qos.logback.core; // Optional, for core features

    opens github.hitman20081.todomanager to javafx.fxml, com.fasterxml.jackson.databind; // Allow Jackson access
    exports github.hitman20081.todomanager;
}
