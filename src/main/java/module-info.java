module ui.level_2_ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ui.level_2_ui.client to javafx.fxml;
    exports ui.level_2_ui.client;
    exports ui.level_2_ui;
    opens ui.level_2_ui to javafx.fxml;
}