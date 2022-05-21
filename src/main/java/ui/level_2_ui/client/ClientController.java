package ui.level_2_ui.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.level_2_ui.message.AuthMessage;
import ui.level_2_ui.message.ChangeNickMessage;
import ui.level_2_ui.message.PrivateMessage;
import ui.level_2_ui.message.SimpleMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public class ClientController {

    private final ChatClient client;

    private String nickTo;

    @FXML
    public TextArea loggerArea;

    @FXML
    private ListView<String> clientList;

    @FXML
    private HBox messageBox;

    @FXML
    private HBox loginBox;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

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
        client.sendMessage(AuthMessage.of(loginField.getText(), passwordField.getText()));

        clearLoginBox();
    }

    public void onSendClick() {
        final String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        if (nickTo != null) {
            client.sendMessage(PrivateMessage.of(nickTo, client.getNick(), message));
            nickTo = null;
        } else if (message.startsWith("/change")){
            final String[] split = message.split("\\s");
            final String newNick = split[1];
            client.sendMessage(ChangeNickMessage.of(client.getNick(), newNick));
        } else {
            client.sendMessage(SimpleMessage.of(message, client.getNick()));
        }
        clearMessageField();
    }

    public void addMessage (String message) {
        messagesArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        loginBox.setVisible(!success);
        messageBox.setVisible(success);

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

    public void showError(String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, error, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Ошибка!");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String nick = clientList.getSelectionModel().getSelectedItem();

            if (nick != null) {
                this.nickTo = nick;
            }

            clearMessageField();
        }
    }

    public void updateClientList(Collection<String> clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    public void setLogger (String log) {
        loggerArea.appendText(log);
    }
}