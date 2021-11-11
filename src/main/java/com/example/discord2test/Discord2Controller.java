package com.example.discord2test;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.Instant;

public class Discord2Controller {

    public static Discord2Loader loader; //a reference to our loader class. this kinda spaghetti code.

    @FXML //the text field on the login screen, that the user types their udername into
    public TextField nameField;

    @FXML //FXML items from the main screen
    public HBox buttonBox;
    public VBox messageBox;
    public TextField messageInputField;

    //JDBC - related member variables
    private Connection connection; //hold a reference to our connection and statement, this way all functions can use
    private Statement statement;   //them, saves overhead.

    //other variables
    private int MessageCount;

    @FXML
    public void initialize() throws SQLException { //set up the connection when we load the form, so the whole
        //script has access to it
        connection = DriverManager.getConnection(Globals.URL, Globals.USERNAME, Globals.PASSWORD);
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        System.out.println("Connected");
        System.out.println(HelperFunctions.GetDatabaseVersion(connection));


        //on launch, fetch all messages to display to the user
        ResultSet rs = statement.executeQuery("select * from messages");
        MessagesTable messages = new MessagesTable(rs);

        messageBox.getChildren().clear();

        for (MessagesRow row : messages.getRows()) {

            DisplayMessage dm = new DisplayMessage(row.Author, row.Text, row.TimeSent); //create a new display message
            messageBox.getChildren().addAll(dm.GetHbox()); //add it onto our document
        }

        //fetch the amount of messages currently and store it in the database
        MessageCount = HelperFunctions.GetMessageCount(statement);

    }

    @FXML
    protected void onSelectButtonClick() {

        try {
            //get all the new messages, and add them to the list
            GetNewMessages();
        } catch (Exception e) {
            System.err.println("You did a silly:\n" + e);
        }
    }

    public void onSubmit() throws SQLException {

        String username = Globals.username;

        MessagesRow mr = new MessagesRow(username, messageInputField.getText(),
                Instant.now(), null, null);
        String InsertStatement = mr.CreateInsertStatement("messages");

        int result = statement.executeUpdate(InsertStatement);
        System.out.printf("Sent to database, return code %d\n", result);

        //this is a good time to refresh the message list, since we have no way of client communication yet
        GetNewMessages();
    }

    public void GetNewMessages() throws SQLException {
        int currcount = HelperFunctions.GetMessageCount(statement);

        if (currcount > MessageCount) {
            //get and display all the new messages we have recieved

            String limitStatement = String.format("SELECT * FROM messages LIMIT %d OFFSET %d",
                    Integer.MAX_VALUE, MessageCount);

            //get a messages table with the response
            MessagesTable diffTable = new MessagesTable(statement.executeQuery(limitStatement));

            //send them to be displayed, with the order to not clear what's already there.
            MessageTableToVbox(diffTable, messageBox, false);
        }
    }

    public void MessageTableToVbox(MessagesTable table, VBox box, boolean clear) {

        if (clear) box.getChildren().clear();

        for (MessagesRow row : table.getRows()) {

            DisplayMessage dm = new DisplayMessage(row.Author, row.Text, row.TimeSent); //create a new display message
            box.getChildren().addAll(dm.GetHbox()); //add it onto our document
        }

    }

}



