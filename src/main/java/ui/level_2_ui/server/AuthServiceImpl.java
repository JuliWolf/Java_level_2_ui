package ui.level_2_ui.server;

import java.util.ArrayList;
import java.util.List;

public class AuthServiceImpl implements AuthService {
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

    public AuthServiceImpl() {
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
        for (Entry client: entries) {
            if (client.login.equals(login) && client.pass.equals(pass)) {
                return client.nick;
            }
        }

        return null;
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
