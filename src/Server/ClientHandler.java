package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;
    private MyServer server;
    private String nickname;

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
                                    if(!server.isNickBusy(nickname)) {
                                        sendMsg("/authOk");
                                        server.subscribe(ClientHandler.this);
                                        server.broadcastMsg(nickname + " вошёл в чат");
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
                            if (msg.equals("/end")) {
                                sendMsg("/serverClosed");
                                break;
                            }
                            if (msg.startsWith("/w")) {
                                sendInterlocutor(msg);
                            } else {
                                server.broadcastMsg(nickname + ": " + msg);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        server.unsubscribe(ClientHandler.this);
                        sendMsg(nickname + "вышел из чата");
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

    void sendInterlocutor(String msg) {
        String[] token = msg.split(" ");
        server.sendPersonalMsg(nickname, token[1], token[2]);
    }

    public String getNickname() {
        return nickname;
    }
}
