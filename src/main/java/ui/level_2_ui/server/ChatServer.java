package ui.level_2_ui.server;

import ui.level_2_ui.message.AbstractMessage;
import ui.level_2_ui.message.ClientListMessage;
import ui.level_2_ui.message.ErrorMessage;
import ui.level_2_ui.message.SimpleMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatServer {
    private final int PORT = 8189;

    private final Map<String, ClientHandler> clients;
    private final DataBaseConnect dbConnection;

    private final ServerLogger logger;

    public ChatServer() {
        this.logger = new ServerLogger();
        this.clients = new HashMap<>();
        this.dbConnection = new DataBaseConnect();
    }

    public void run () {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try (
            ServerSocket server = new ServerSocket(PORT);
            AuthService authService = new AuthServiceImpl(dbConnection)
        ) {
            while (true) {
                logger.info("Wait client connection...");
                Socket socket = server.accept();
                new ClientHandler(socket, this, authService, executorService, logger);
                logger.info("Client connected");
            }
        } catch (IOException e) {
            logger.error("Ошибка в работе сервера");
        } finally {
            executorService.shutdownNow();
        }
    }

    public synchronized boolean isNickBusy (String nick) {
        return clients.containsKey(nick);
    }

    public synchronized void broadcast (AbstractMessage message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
            logger.info(message);
        }
    }

    public void sendPrivateMessage(ClientHandler sender, String to, String message) {
        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage(SimpleMessage.of("от " + sender.getNick() + ": " + message, sender.getNick()));
            sender.sendMessage(SimpleMessage.of("участнику " + to + ": " + message, sender.getNick()));
        } else {
            sender.sendMessage(ErrorMessage.of("Участника с ником " + to + " нет в чате!"));
        }
    }

    public synchronized void unsubscribe (ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
        logger.debug("Client with nick: " + client.getNick() + " was unsubscribed");
    }

    public synchronized void subscribe (ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
        logger.debug("Client with nick: " + client.getNick() + " was subscribed");
    }

    private void broadcastClientList() {
        final List<String> nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.toList());
        broadcast(ClientListMessage.of(nicks));
    }

    public void changeNick(String nick, String newNick) {
        dbConnection.changeNick(nick, newNick);
        broadcastClientList();
        broadcast(SimpleMessage.of( "Nick changed. New nick is " + newNick, nick));
    }
}
