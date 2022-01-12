package com.example.discord2test;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Pattern;

public class LoginController {

    private static final int INIT_PORT_VALUE = 54321;
    @FXML
    public TextField nameField;
    public TextField ipField;
    public TextField portField;

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
        Globals.loader.changeScene("mainScreen.fxml");
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode() == KeyCode.ENTER) onConnect();
    }
}
