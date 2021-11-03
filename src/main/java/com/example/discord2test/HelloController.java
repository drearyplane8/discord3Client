package com.example.discord2test;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.sql.*;

public class HelloController {
    public Label text;

    @FXML
    public Text resultsText;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onSelectButtonClick() {

        try(Connection connection = DriverManager.getConnection(Credentials.URL, Credentials.USERNAME, Credentials.PASSWORD)){

            System.out.println("Connected");
            System.out.println(HelperFunctions.GetDatabaseVersion(connection));

            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet rs = statement.executeQuery("select * from messages");
            MessagesTable messages = new MessagesTable(rs);

            resultsText.setText(messages.toNiceString());

        } catch (Exception e) {
            System.err.println("You did a silly:\n" + e);
        }


    }
}



