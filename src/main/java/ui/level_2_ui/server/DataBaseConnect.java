package ui.level_2_ui.server;

import java.sql.*;

public final class DataBaseConnect {
    private Connection connection;

    public DataBaseConnect () {
        try {
            this.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connect () throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
    }

    public void closeConnection () throws SQLException {
        connection.close();
    }

    public String getNickByLoginPass (String login, String password) {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?")) {
            statement.setString(1, login);
            statement.setString(2, password);

            final ResultSet rs = statement.executeQuery();
            return rs.getString("nick");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void changeNick (String currentNick, String newNick) {
        try (final PreparedStatement statement = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?")) {
            statement.setString(1, newNick);
            statement.setString(2, currentNick);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
