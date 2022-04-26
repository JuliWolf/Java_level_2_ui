package ui.level_2_ui.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ClientController {

    private final ChatClient client;

    @FXML
    private TextArea textArea;

    @FXML
    private HBox loginBox;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private VBox messageBox;

    @FXML
    private TextArea messagesArea;

    @FXML
    private TextField messageField;

    public ClientController () {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (Exception e) {
                showNotification();
            }
        }
    }

    public void onAuthClick(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();

        client.sendMessage("/auth " + login + " " + password);

        clearLoginBox();
    }

    public void onSendClick() {
        final String userMessage = messageField.getText();

        if (userMessage.isEmpty()) return;

        client.sendMessage(userMessage);
        clearMessageField();
    }

    public void addMessage (String message) {
        messagesArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        loginBox.setVisible(!success);
        messageBox.setVisible(success);
        textArea.setVisible(success);

        if (!success) {
            Platform.exit();
        }
    }

    private void showNotification() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключится к серверу.\n" +
                        "Проверьте, что сервер запущен",
                new ButtonType("Попробовать еще", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Ошибка подключения");
        final Optional<ButtonType> buttonType = alert.showAndWait();
        final Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }

    private void clearMessageField () {
        messageField.clear();
        messageField.requestFocus();
    }

    private void clearLoginBox () {
        loginField.clear();
        passwordField.clear();
    }
}