package com.example.discord2test;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jdk.jfr.Description;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class Discord2Controller {

    @FXML //FXML items from the main screen
    public HBox buttonBox;
    public VBox messageBox;
    public TextField messageInputField;

    //JDBC - related member variables
    private Connection connection; //hold a reference to our connection and statement, this way all functions can use
    private Statement statement;   //them, saves overhead.

    //other variables
    private int MessageCount;

    //we're going to use a dictionary to hold our messages, so accessing them by message ID is an O(1) operation
    //but java is a  so Dictionary is obsolete, we're using a map
    private final HashMap<Integer, ProcessedMessage> ProcessedMessages = new HashMap<>();

    @FXML
    public void initialize() throws SQLException {

        //set up the connection when we load the form, so the whole
        //script has access to it
        connection = DriverManager.getConnection(Globals.URL, Globals.USERNAME, Globals.PASSWORD);
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        System.out.println("Connected");
        System.out.println(HelperFunctions.GetDatabaseVersion(connection));


        //on launch, fetch all messages to display to the user
        ResultSet rs = statement.executeQuery("select * from messages");
        MessagesTable messages = new MessagesTable(rs);

        //call the function to wrap the table up into a vbox and display it
        DisplayMessageTableInVBox(messages, messageBox, true);

        //fetch the amount of messages currently and store it
        MessageCount = HelperFunctions.GetMessageCount(statement);

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

        //get the amount of messages currently in the database
        int currcount = HelperFunctions.GetMessageCount(statement);

        //if the amount from the database is greater than our client's idea of how many messages have been sent
        if (currcount > MessageCount) {

            //get and display all the new messages we have received
            String limitStatement = String.format("SELECT * FROM messages LIMIT %d OFFSET %d",
                    Integer.MAX_VALUE, MessageCount);

            //get a messages table with the response
            MessagesTable diffTable = new MessagesTable(statement.executeQuery(limitStatement));

            //send them to be displayed, with the order to not clear what's already there.
            DisplayMessageTableInVBox(diffTable, messageBox, false);
        }
    }

    public void DisplayMessageTableInVBox(MessagesTable table, VBox box, boolean clear) {

        if (clear) box.getChildren().clear();

        for (MessagesRow row : table.getRows()) {

            ProcessedMessage pm = new ProcessedMessage(row, this); //create a new processed message

            //if the row has the same author as our current username, it will be coloured red. otherwise, blue
            pm.setColours(row.Author.equals(Globals.username));

            box.getChildren().addAll(pm.GetHbox()); //add it onto our document

            //store the processed message in the hashmap with the message ID as the key
            ProcessedMessages.put(pm.getMessageID(), pm);
        }

    }

    @Description("If a key is pressed in the message field, if its enter, forward to onSubmit()")
    public void onKeyPressed_MessageField(KeyEvent keyEvent) throws SQLException {
        if (keyEvent.getCode() == KeyCode.ENTER) onSubmit();
    }

    //a lot of code is copy-pasted here, there's likely a more efficient way of doing it but can I be bothered?
    public void onUpvote(int MessageID) {
        try {
            //get the VoteButtons class from the PM class our ID refers to, its all we need
            VoteButtons buttons = ProcessedMessages.get(MessageID).getButtons();

            if (buttons.isUpPressed()) {
                //button has been pressed, un-upvote
                System.out.printf("Un-upvoting message%d\n", MessageID);
                UpdateDatabaseAboutVote(MessageID, false);
            } else {
                //button has not been pressed, upvote
                System.out.printf("Upvoting message %d\n", MessageID);
                UpdateDatabaseAboutVote(MessageID, true);
            }
            //flip the boolean and change the colours
            buttons.setUpPressed(!buttons.isUpPressed());
            UpdateButtonColours(buttons);

        } catch (Exception e) {
            System.err.println("done oopsy in upvote" + e);
        }
    }

    public void onDownvote(int MessageID) {
        try {
            //get the VoteButtons class from the PM class our ID refers to, its all we need
            VoteButtons buttons = ProcessedMessages.get(MessageID).getButtons();

            if (buttons.isDownPressed()) {
                //button has been pressed, un-downvote
                System.out.printf("Un-downvote message%d\n", MessageID);
                UpdateDatabaseAboutVote(MessageID, true);
            } else {
                //button has not been pressed, upvote
                System.out.printf("downvoting message %d\n", MessageID);
                UpdateDatabaseAboutVote(MessageID, false);
            }
            //flip the boolean and change the colours
            buttons.setDownPressed(!buttons.isDownPressed());
            UpdateButtonColours(buttons);

        } catch (Exception e) {
            System.err.println("done oopsy in upvote" + e);
        }
    }

    public void UpdateButtonColours(VoteButtons buttons){

        if (buttons.isUpPressed()){ //if the upvote button has been pressed, it should be orange.
            buttons.getUp().setStyle("-fx-background-color: orange");
        } else { //otherwise it should be grey
            buttons.getUp().setStyle(null);
        }

        if(buttons.isDownPressed()){ //if the downvote button is pressed, it should be blue
            buttons.getDown().setStyle("-fx-background-color: blue");
        }else { //otherwise, it should be grey
            buttons.getDown().setStyle(null);
        }
    }

    public void UpdateDatabaseAboutVote(int MessageID, boolean positive) throws SQLException {

        //firstly, see if we're upvoting or downvoting.
        int voteToAdd;
        if (positive) voteToAdd = 1;
        else voteToAdd = -1;

        //generate an update statement
        final String updateStatement = String.format("UPDATE messages SET VoteSum = VoteSum + %d WHERE MessageID = %d", voteToAdd, MessageID);

        statement.executeUpdate(updateStatement);
    }
}



