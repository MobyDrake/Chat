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
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
        @FXML
        ListView<String> listClients;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;
    private boolean isAuth;
    //private final int timeToDisconnect = 120000;
    //private Timer timer = new Timer();
    //private boolean timerRun = false;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public void setAuth(boolean isAuth) {
        this.isAuth = isAuth;

        if(!isAuth) {
            borderAuth.setVisible(true);
            borderAuth.setManaged(true);
            borderAuth.setDisable(false);

            borderChat.setVisible(false);
            borderChat.setManaged(false);
            borderChat.setDisable(true);
        } else {
            borderAuth.setVisible(false);
            borderAuth.setManaged(false);
            borderAuth.setDisable(true);

            borderChat.setVisible(true);
            borderChat.setManaged(true);
            borderChat.setDisable(false);
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
                        //цикл авторизации
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

                        //цикл получения сообщений
                        while (true) {
                            String text = in.readUTF();
                            //поток обновления интерфейса
                            if (text.startsWith("/")) {
                                if (text.startsWith("/serverClosed")) {
                                    setAuth(false);
                                }
                                if (text.startsWith("/listClients")) {
                                    String[] token = text.split(" ");

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            listClients.getItems().clear();
                                            for (int i = 1; i < token.length; i++) {
                                                listClients.getItems().add(token[i]);
                                            }
                                        }
                                    });
                                }
                                if (text.startsWith("/History")) {
                                    String[] token = text.split("/h");

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatBox.setSpacing(10);
                                            for (int i = 1; i < token.length; i++) {
                                                chatBox.getChildren().add(new MessageTextArea(token[i]));
                                            }
                                        }
                                    });
                                }
                            } else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatBox.setSpacing(10);
                                        chatBox.getChildren().add(new MessageTextArea(text));
                                    }
                                });
                            }
//                            if (!timerRun) {
//                                timerToDisconnect();
//                            }
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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    label.setText("Проблемы с сервером...");
                }
            });
        }
    }

    public void sendMsg() {
        //таймер
//        timer.cancel();
//        timerRun = false;
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

//    public void timerToDisconnect() {
//        //попробовать следующие решение
//        //http://pro-java.ru/java-dlya-opytnyx/tajm-aut-soketov-java/
//        timerRun = true;
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    out.writeUTF("/end");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, timeToDisconnect, timeToDisconnect);
//    }

}
