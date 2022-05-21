package ui.level_2_ui.server;

import ui.level_2_ui.message.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessageLogger messageLogger;
    AuthService authService;

    private String nick;

    private final int AUTH_TIMEOUT = 120;
    private final Future<Void> timeoutFuture;

    private final ServerLogger logger;

    public String getNick () {
        return nick;
    }

    public ClientHandler(
            Socket socket,
            ChatServer server,
            AuthService authService,
            ExecutorService executorService,
            ServerLogger logger
    ) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.authService = authService;
            this.logger = logger;

            executorService.execute(() -> {
                try {
                    authentication();
                    readMessage();
                } finally {
                    closeConnection();
                }
            });

            timeoutFuture = executorService.submit(() -> {
                try {
                    Thread.sleep(AUTH_TIMEOUT * 1000);
                    logger.info("Time is over, stop client");
                    closeConnection();
                } catch (InterruptedException e) {
                    // do nothing
                }
                return null;
            });
        } catch (Exception e) {
            logger.fatal("Проблемы при создании обработчика клиента");
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private void authentication() {
        while (true) {
            try {
                final AbstractMessage message = (AbstractMessage) in.readObject();
                if (message.getCommand() == Command.AUTH) {
                    final AuthMessage authMessage = (AuthMessage) message;
                    final String login = authMessage.getLogin();
                    final String password = authMessage.getPassword();
                    final String nick = authService.getNickByLoginPass(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(ErrorMessage.of("Пользователь уже авторизован"));
                            continue;
                        }

                        this.timeoutFuture.cancel(true);
                        sendMessage(AuthOkMessage.of(nick));

                        initLogger(login);

                        this.nick = nick;

                        server.broadcast(SimpleMessage.of(nick, "Пользователь " + nick + " зашел в чат"));
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage(ErrorMessage.of("Неверные логин и пароль"));
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(AbstractMessage message) {
        try {
            logger.info("SERVER: Send message type " + message.getCommand());
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initLogger (String login) {
        this.messageLogger = new MessageLogger(login);
        sendMessage(LogMessage.of(messageLogger.read()));
    }

    private void readMessage() {
        try {
            while (true) {
                final AbstractMessage message = (AbstractMessage) in.readObject();
                logger.info("Receive message: " + message);

                if (message.getCommand() == Command.AUTH_TIMEOUT) {
                    break;
                }

                if (message.getCommand() == Command.END) {
                    break;
                }

                if (message.getCommand() == Command.MESSAGE) {
                    final SimpleMessage simpleMessage = (SimpleMessage) message;
                    server.broadcast(simpleMessage);
                    messageLogger.write("message: " + simpleMessage.getMessage());
                }

                if (message.getCommand() == Command.PRIVATE_MESSAGE) {
                    final PrivateMessage privateMessage = (PrivateMessage) message;
                    server.sendPrivateMessage(this, privateMessage.getNickTo(), privateMessage.getMessage());
                    messageLogger.write("private message to " + privateMessage.getNickTo() + ": " + privateMessage.getMessage());
                }

                if (message.getCommand() == Command.CHANGE_NICK) {
                    final ChangeNickMessage changeNickMessage = (ChangeNickMessage) message;
                    String newNick = changeNickMessage.getNewNick();
                    server.changeNick(nick, newNick);
                    sendMessage(ChangeNickMessage.of(nick, newNick));
                    nick = newNick;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        sendMessage(EndMessage.of());
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
