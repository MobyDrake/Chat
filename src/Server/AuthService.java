package Server;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        String sql = String.format("SELECT nickname FROM main where login = '%s' and password = '%s'", login, password);
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

    public static boolean addNickBlackListSQL(String nick, String badBoy) {
        ArrayList<String> list = getBlackListSQL(nick);
        if(!list.contains(badBoy)) {
            String sql = String.format("INSERT INTO blackList(nickname, badBoy)\n" +
                    "VALUES('%s', '%s')", nick, badBoy);
            try {
                statement.execute(sql);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static ArrayList<String> getBlackListSQL(String nick) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "select * from blackList";
        try {
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                if (rs.getString("nickname").equals(nick)) {
                    list.add(rs.getString("badBoy"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void addMsgHistory(String nickname, String msg) {
        String sql = String.format("INSERT INTO history(nickname, message)\n" +
                "VALUES('%s', '%s')", nickname, msg);
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getHistoryMsg() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "select * from history";

        try {
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                list.add(rs.getString("message"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


}
