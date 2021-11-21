package com.example.discord2test;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

public class LoginController {

    @FXML
    public TextField nameField;

    public void onConnect() throws IOException {
        Globals.username = nameField.getText();
        Globals.loader.changeScene("mainScreen.fxml");
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode() == KeyCode.ENTER) onConnect();
    }
}
