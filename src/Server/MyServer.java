package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class MyServer {
    private final int PORT = 8189;
    private Vector<ClientHandler> clients;

    public MyServer() throws SQLException {
        ServerSocket server = null;
        Socket socket = null;
        clients = new Vector<>();

        //добавить try-resource для server
        try {
            AuthService.connect();
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            System.out.println("Ошибка работы сервера...");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        for(ClientHandler cl : clients) {
            if (!cl.checkBlackList(from.getNickname())) {
                cl.sendMsg(msg);
            }
        }
    }

    public void sendPersonalMsg(String nickSending, String nickReceiver, String msg) {
        for (ClientHandler cl : clients) {
            if (cl.getNickname().equals(nickReceiver) && !cl.checkBlackList(nickSending) ) {
                cl.sendMsg(String.format("Личное от %s: %s", nickSending, msg));
            }
            if (cl.getNickname().equals(nickSending)) {
                cl.sendMsg(String.format("Личное для %s: %s", nickReceiver, msg));
            }
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/listClients ");

        for(ClientHandler cl : clients) {
            sb.append(cl.getNickname()).append(" ");
        }

        for(ClientHandler cl : clients) {
            cl.sendMsg(sb.toString());
        }
    }

    public void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientsList();
    }

    public boolean hasClient(String nick) {
        for (ClientHandler cl : clients) {
            if (cl.getNickname().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void sendHistory(ClientHandler o) {
        StringBuilder sb = new StringBuilder("/History/h");

        for(String str : AuthService.getHistoryMsg()) {
            sb.append(str).append("/h");
        }

        o.sendMsg(sb.toString());
    }

}
