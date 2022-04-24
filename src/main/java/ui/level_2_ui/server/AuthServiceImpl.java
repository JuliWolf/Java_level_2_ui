package ui.level_2_ui.server;

import java.util.ArrayList;
import java.util.List;

public class AuthServiceImpl implements AuthService {
    private final DataBaseConnect dbConnection;

    private class Entry {
        private final String login;
        private final String pass;
        private final String nick;


        private Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<Entry> entries;

    public AuthServiceImpl(DataBaseConnect connect) {
        this.dbConnection = connect;
        entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            entries.add(new Entry("login"+i, "pass"+i, "nick"+i));
        }
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
