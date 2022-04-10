package ui.level_2_ui.server;

import java.io.Closeable;
import java.io.IOException;

public interface AuthService extends Closeable {
    void start();

    String getNickByLoginPass (String login, String pass);

    @Override
    void close() throws IOException;
}
