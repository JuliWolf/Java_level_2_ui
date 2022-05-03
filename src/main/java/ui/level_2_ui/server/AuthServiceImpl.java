package ui.level_2_ui.server;

public class AuthServiceImpl implements AuthService {
    private final DataBaseConnect dbConnection;

    public AuthServiceImpl(DataBaseConnect connect) {
        this.dbConnection = connect;
    }

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        return dbConnection.getNickByLoginPass(login, pass);
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
