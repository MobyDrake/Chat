package Server;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement statement;


    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DBUsers.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNicknameByLoginAndPass(String login, String password) {
        String sql = String.format("select nickname from main where login = '%s' and password = '%s'", login, password);
        try {
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
