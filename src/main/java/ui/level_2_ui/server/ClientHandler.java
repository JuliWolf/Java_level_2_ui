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
    private Boolean isAuthExpired = false;
    private Boolean isAuthenticated = false;

    private ExecutorService service;

    public String getNick () {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.authService = authService;

            this.service = Executors.newFixedThreadPool(2);

            service.execute(() -> {
                try {
                    Future<?> future = service.submit(authentication());
                    future.get(AUTH_TIMEOUT, TimeUnit.SECONDS);
                } catch (ExecutionException | TimeoutException | InterruptedException e) {
                    if (!this.isAuthenticated) {
                        this.isAuthExpired = true;
                        sendMessage(AuthTimeoutMessage.of());
                    }
                }
            });

            service.execute(() -> {
                try {
                    readMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });

            service.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private Runnable authentication() {
        return () -> {
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

                            sendMessage(AuthOkMessage.of(nick));

                            initLogger(login);

                            this.nick = nick;
                            this.isAuthenticated = true;

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
        };
    }

    public void sendMessage(AbstractMessage message) {
        try {
            System.out.println("SERVER: Send message type " + message.getCommand());
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initLogger (String login) {
        this.messageLogger = new MessageLogger(login);
        sendMessage(LogMessage.of(messageLogger.read()));
    }

    private void readMessage() throws Exception {
        try {
            while (true) {
                if (isAuthExpired) break;

                final AbstractMessage message = (AbstractMessage) in.readObject();
                System.out.println("Receive message: " + message);

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
        } catch (IOException e) {
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

        service.shutdown();
    }
}
