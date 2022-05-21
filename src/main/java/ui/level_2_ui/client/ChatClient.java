package ui.level_2_ui.client;

import javafx.application.Platform;
import ui.level_2_ui.message.*;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nick;

    private ClientController controller;

    private boolean isAuthExpired = false;

    public ChatClient (ClientController controller) {
        this.controller = controller;
    }

    public String getNick() {
        return nick;
    }

    public void openConnection () throws IOException {
        socket = new Socket("localhost", 8189);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });

        readThread.setDaemon(true);
        readThread.start();
    }

    private void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private void readMessage() throws Exception {
        while (true) {
            if (isAuthExpired) break;

            final AbstractMessage message = (AbstractMessage) in.readObject();
            System.out.println("Receive message: " + message);

            if (message.getCommand() == Command.END) {
                controller.setAuth(false);
                break;
            }

            if (message.getCommand() == Command.ERROR) {
                final ErrorMessage errorMessage = (ErrorMessage) message;
                Platform.runLater(() -> controller.showError(errorMessage.getError()));
            } else if (message.getCommand() == Command.CLIENTS) {
                final ClientListMessage clientListMessage = (ClientListMessage) message;
                controller.updateClientList(clientListMessage.getClients());
            } else if (message.getCommand() == Command.MESSAGE) {
                final SimpleMessage simpleMessage = (SimpleMessage) message;
                controller.addMessage(simpleMessage.getNickFrom() + ": " + simpleMessage.getMessage());
            } else if (message.getCommand() == Command.CHANGE_NICK) {
                this.nick = ((ChangeNickMessage) message).getNewNick();
            } else if (message.getCommand() == Command.LOG) {
                final LogMessage logMessage = (LogMessage) message;
                controller.setLogger(logMessage.getLog());
            }
        }
    }

    private void waitAuthenticate() throws Exception {
        while (true) {
            final AbstractMessage message = (AbstractMessage) in.readObject();
            if (message.getCommand() == Command.AUTHOK) {
                this.nick = ((AuthOkMessage) message).getNick();
                controller.addMessage("Успешная авторизация под ником " + nick);
                controller.setAuth(true);
                break;
            }

            if (message.getCommand() == Command.ERROR) {
                final ErrorMessage errorMessage = (ErrorMessage) message;
                Platform.runLater(() -> controller.showError(errorMessage.getError()));
            }

            if (message.getCommand() == Command.AUTH_TIMEOUT) {
                isAuthExpired = true;
                controller.setAuth(false);
                sendMessage(message);
                break;
            }
        }
    }

    public void sendMessage(AbstractMessage message) {
        try {
            System.out.println("Send message: " + message);
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
