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
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/auth")) {
                                String[] token = str.split(" ");
                                nickname = AuthService.getNicknameByLoginAndPass(token[1], token[2]);
                                if (nickname != null) {
                                    sendMsg("/authOk");
                                    server.subscribe(ClientHandler.this);
                                    server.broadcastMsg(nickname + "вошёл в чат");
                                    break;
                                } else {
                                    sendMsg("Неверный логи или пароль");
                                }
                            }

                        }

                        while(true) {
                            String text = in.readUTF();
                            if (text.equals("/end")) {
                                sendMsg("/serverClosed");
                                break;
                            }
                            server.broadcastMsg(nickname + ": " +text);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
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
                        server.unsubscribe(ClientHandler.this);
                        sendMsg(nickname + "вышел из чата");
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
}
