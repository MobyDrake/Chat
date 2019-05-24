package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    VBox chatBox;

    @FXML
    TextArea msgText;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String text = in.readUTF();
                            //поток обновления интерфейса
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
//                chatBox.setSpacing(10);
//                chatBox.getChildren().add(new MessageTextArea(msgText.getText()));
                out.writeUTF(msgText.getText().trim());
                msgText.clear();
//              msgText.requestFocus(); //не сбрасывает фокус с поля
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
