package ui.level_2_ui.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private final int PORT = 8189;

    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run () {
        try (
            ServerSocket server = new ServerSocket(PORT);
            AuthService authService = new AuthServiceImpl()
        ) {
            while (true) {
                System.out.println("Wait client connection...");
                Socket socket = server.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера");
        }
    }

    public synchronized boolean isNickBusy (String nick) {
        return clients.containsKey(nick);
    }

    public synchronized void broadcast (String message) {
        clients.values().forEach(client -> client.sendMessage(message));
    }

    public void broadcastPrivate(String from, String to, String message) {
        ClientHandler sender = clients.get(from);
        ClientHandler receiver = clients.get(to);

        if (receiver == null) {
            sender.sendMessage("SERVER: user with nick " + to + " is not active");
            return;
        }

        receiver.sendMessage(message);
    }

    public synchronized void unsubscribe (ClientHandler client) {
        clients.remove(client.getNick());
    }

    public synchronized void subscribe (ClientHandler client) {
        clients.put(client.getNick(), client);
    }
}
