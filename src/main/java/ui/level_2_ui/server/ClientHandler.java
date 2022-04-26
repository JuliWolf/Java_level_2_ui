package ui.level_2_ui.server;

import ui.level_2_ui.server.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    AuthService authService;

    private String nick;
    private final int AUTH_TIMEOUT = 120;
    private Boolean isAuthExpired = false;
    private Boolean isAuthenticated = false;

    public String getNick () {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            new Thread(() -> {
                try {
                    Thread.sleep(AUTH_TIMEOUT * 1000);

                    if (!isAuthenticated) {
                        sendMessage("/authTimeout");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void authentication() throws IOException {
        while (true) {
            try {
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s");
                    String login = parts[1];
                    String password = parts[2];
                    String nick = authService.getNickByLoginPass(login, password);

                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }

                        this.nick = nick;
                        sendMessage("/authok " + nick);
                        this.isAuthenticated = true;
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);
                        break;

                    } else {
                        sendMessage("Неверные логин/пароль");
                    }
                }

                if ("/authTimeout".equals(str)) {
                    isAuthExpired = true;
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("SERVER: Send message to " + nick);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() throws IOException {
        try {
            while (true) {
                if (isAuthExpired) break;

                String message = in.readUTF();
                System.out.println("Receive message: " + message);

                if (message.startsWith("/w")) {
                    String[] split = message.split("\\s");
                    String receiver = split[1];
                    String msg = split[2];
                    server.sendPrivateMessage(nick, receiver, msg);
                    continue;
                }

                if (message.startsWith("/change")) {
                    String[] split = message.split("\\s");
                    String newNick = split[1];
                    server.changeNick(nick, newNick);
                    continue;
                }

                if ("/end".equals(message)) {
                    break;
                }

                server.broadcast(nick + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcast(nick + " has leaved chat");

        sendMessage("/end");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
