package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

        @FXML
        BorderPane borderAuth;

        @FXML
        BorderPane borderChat;

        @FXML
        TextField loginField;

        @FXML
        PasswordField passwordField;

        @FXML
        Button btnLogin;

        @FXML
        VBox chatBox;

        @FXML
        TextArea msgText;
        @FXML
        Label label;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;
    private boolean isAuth;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public void setAuth(boolean isAuth) {
        this.isAuth = isAuth;

        if(!isAuth) {
            borderAuth.setVisible(true);
            borderAuth.setManaged(true);

            borderChat.setVisible(false);
            borderChat.setManaged(false);
        } else {
            borderAuth.setVisible(false);
            borderAuth.setManaged(false);

            borderChat.setVisible(true);
            borderChat.setManaged(true);
        }
    }

    public void connectClient() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.equals("/authOk")) {
                                setAuth(true);
                                break;
                            } else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        label.setText(str);
                                    }
                                });
                            }
                        }

                        while (true) {
                            String text = in.readUTF();
                            //поток обновления интерфейса
                            //проверить небходимость if
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    chatBox.setSpacing(10);
                                    chatBox.getChildren().add(new MessageTextArea(text));
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
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

    public void sendMsg() {
        try {
            if (!msgText.getText().isEmpty()) {
                out.writeUTF(msgText.getText().trim());
                msgText.clear();
//              msgText.requestFocus(); //не сбрасывает фокус с поля
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void authClient() {
        if (socket == null || socket.isClosed()) {
            connectClient();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
