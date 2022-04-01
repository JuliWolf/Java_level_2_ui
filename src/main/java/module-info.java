module ui.level_2_ui {
    requires javafx.controls;
    requires javafx.fxml;


    opens ui.level_2_ui to javafx.fxml;
    exports ui.level_2_ui;
}