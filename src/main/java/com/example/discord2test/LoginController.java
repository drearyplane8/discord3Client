package com.example.discord2test;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class LoginController {

    private static final int INIT_PORT_VALUE = 54321;
    @FXML
    public TextField nameField;
    public TextField ipField;
    public TextField portField;
    public Button connectButton;

    private boolean buttonErrorState = false;

    public void initialize(){
        //thanks Emily from StackOverflow for this monstrous one-liner
        //it means we can only type integers into the port field.
        TextFormatter<Integer> formatter = new TextFormatter<>(
                new IntegerStringConverter(),
                INIT_PORT_VALUE,
                c -> Pattern.matches("\\d*", c.getText()) ? c : null);
        portField.setTextFormatter(formatter);
    }

    public void onConnect() throws IOException {

        Globals.IP = ipField.getText();

        //no need for try-catch as the field can only contain integers.
        Globals.port = Integer.parseInt(portField.getText());

        Globals.username = nameField.getText();

        try{
            //the thingy worked.
            Globals.socket = new Socket(Globals.IP, Globals.port);

        } catch (UnknownHostException uhe) {

            //the ip is wrong.
            ChangeConnectButton("Incorrect IP", true);
            return;

        } catch (IOException ioe) {

            //some other error has happened setting up the socket
            ChangeConnectButton("Error", true);
            return;

        } catch (IllegalArgumentException iae ) {
            //the port is not within valid reach
            ChangeConnectButton("Invalid Port", true);
            return;
        }

        Globals.loader.changeScene("mainScreen.fxml");
        //any code below here is not reached
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode() == KeyCode.ENTER) onConnect();
    }

    public void ChangeConnectButton(String text, boolean error) {
        connectButton.setText(text);
        connectButton.setStyle(
                String.format("-fx-text-fill: %s; -fx-font-size: 20; -fx-font-weight: bold;",
                        error ? "red" : "black" ));
        buttonErrorState = error;
    }

    public void onIPFieldChanged() {
        if(buttonErrorState) {
            ChangeConnectButton("Connect", false);
        }
    }
}
