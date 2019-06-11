package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class ClientHandler {
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;
    private MyServer server;
    private String nickname;
    private ArrayList<String> blackList;

    public ClientHandler(Socket socket, MyServer server) {
        try {
            this.socket = socket;
            this.server = server;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //аторизация
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/auth")) {
                                String[] token = str.split(" ");
                                nickname = AuthService.getNicknameByLoginAndPass(token[1], token[2]);
                                if (nickname != null) {
                                    if(!server.hasClient(nickname)) {
                                        sendMsg("/authOk");
                                        server.subscribe(ClientHandler.this);
                                        server.sendHistory(ClientHandler.this);
                                        server.broadcastMsg(ClientHandler.this, nickname + " вошёл в чат");
                                        break;
                                    } else {
                                        sendMsg("Учётная запись уже используется");
                                    }
                                } else {
                                    sendMsg("Неверный логи или пароль");
                                }
                            }

                        }

                        //отправка сообщений
                        while(true) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/")) {
                                if (msg.equals("/end")) {
                                    sendMsg("/serverClosed");
                                    break;
                                }
                                if (msg.startsWith("/w")) {
                                    sendInterlocutor(msg);
                                }
                                if (msg.startsWith("/blacklist")) {
                                    addBlackList(msg);
                                }
                            } else {
                                Calendar calendar = Calendar.getInstance();
                                String str = String.format("%tl:%tM %tp %s: %s", calendar, calendar, calendar, nickname, msg);
                                AuthService.addMsgHistory(nickname, str);
                                server.broadcastMsg(ClientHandler.this, str);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        server.unsubscribe(ClientHandler.this);
                        server.broadcastMsg(ClientHandler.this,nickname + " вышел из чата");
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInterlocutor(String msg) {
        String[] token = msg.split(" ", 3);
        server.sendPersonalMsg(nickname, token[1], token[2]);
    }

    public void addBlackList(String msg) {
        String[] token = msg.split(" ");
        if (server.hasClient(token[1])) {
            if (AuthService.addNickBlackListSQL(nickname, token[1])) {
                sendMsg(String.format("Вы добавили %s в чёрный список", token[1]));
            }
        } else {
            sendMsg("Такого пользователся нет");
        }
    }

    public boolean checkBlackList(String nickname) {
        this.blackList = AuthService.getBlackListSQL(this.nickname);
        return this.blackList.contains(nickname);
    }

    public String getNickname() {
        return nickname;
    }
}
