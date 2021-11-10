package com.example.discord2test;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.Instant;

public class Discord2Controller {


    public HBox buttonBox;
    public VBox messageBox;

    private Connection connection; //hold a reference to our connection and statement, this way all functions can use
    private Statement statement;   //them, saves overhead.

    @FXML
    public TextField messageInputField;

    @FXML
    public void initialize() throws SQLException { //set up the connection when we load the form, so the whole
        //script has access to it
        connection = DriverManager.getConnection(Credentials.URL, Credentials.USERNAME, Credentials.PASSWORD);
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        System.out.println("Connected");
        System.out.println(HelperFunctions.GetDatabaseVersion(connection));
    }

    @FXML
    protected void onSelectButtonClick() {

        try {

            ResultSet rs = statement.executeQuery("select * from messages");
            MessagesTable messages = new MessagesTable(rs);

            for(MessagesRow row : messages.getRows()){

                DisplayMessage dm = new DisplayMessage(row.Author, row.Text, row.TimeSent); //create a new display message
                messageBox.getChildren().addAll(dm.GetHbox()); //add it onto our document
            }



        } catch (Exception e) {
            System.err.println("You did a silly:\n" + e);
        }
    }

    public void onSubmit() throws SQLException {

        MessagesRow mr = new MessagesRow("placeholder", messageInputField.getText(), Instant.now(), null, null);
        String InsertStatement = mr.CreateInsertStatement("messages");

        int result = statement.executeUpdate(InsertStatement);
        System.out.printf("Sent to database, return code %d\n", result);
    }
}



