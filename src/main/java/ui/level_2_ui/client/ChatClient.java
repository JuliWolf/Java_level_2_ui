package ui.level_2_ui.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private ClientController controller;

    private boolean isAuthExpired = false;

    public ChatClient (ClientController controller) {
        this.controller = controller;
    }

    public void openConnection () throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            if (isAuthExpired) break;

            String message = in.readUTF();
            System.out.println("Receive message: " + message);

            if ("/end".equals(message)) {
                controller.setAuth(false);
                break;
            }
            controller.addMessage(message);
        }
    }

    private void waitAuthenticate() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if (message.startsWith("/authok")) {
                final String[] split = message.split("\\s");
                final String nick = split[1];
                controller.addMessage("Успешная авторизация под ником " + nick);
                controller.setAuth(true);
                break;
            }

            if ("/authTimeout".equals(message)) {
                isAuthExpired = true;
                controller.setAuth(false);
                sendMessage("/authTimeout");
                break;
            }
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
