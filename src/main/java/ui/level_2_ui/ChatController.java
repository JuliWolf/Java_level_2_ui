package ui.level_2_ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {
    @FXML
    private TextArea messagesArea;

    @FXML
    private TextField messageField;

    @FXML
    private void onSendClick() {
        final String userMessage = messageField.getText();

        if (userMessage.isEmpty()) return;

        final String text = generateString(userMessage, "User says:", "");
        messagesArea.appendText(text);
        clearField();
    }

    private String generateString (String text, String append, String prepend) {
        return append + " " + text + " " + prepend + "\n";
    }

    private void clearField () {
        messageField.clear();
    }
}