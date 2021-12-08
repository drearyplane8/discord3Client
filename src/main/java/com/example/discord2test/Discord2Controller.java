package com.example.discord2test;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.text.Text;
import jdk.jfr.Description;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class Discord2Controller {

    @FXML //FXML items from the main screen
    public HBox buttonBox;
    public VBox messageBox;
    public TextField messageInputField;
    public BorderPane SearchPane;

    //a list of everything we want to invisible when the scroll pane pulls up
    public ArrayList<Node> mainMessageNodes = new ArrayList<>();
    //and a list of everything that makes up the search pane
    public ArrayList<Node> searchPaneNodes = new ArrayList<>();

    //All the text fields in the search section
    public TextField keywordInputField,
            userInputField,
            likesLowerBoundInputField,
            likesUpperBoundInputField;

    public DatePicker
            dateLowerBoundInputField,
            dateUpperBoundInputField;


    //the radio buttons in the search section
    public RadioButton timeSentNewestToOldestButton;
    public RadioButton timeSentOldestToNewestButton;
    public RadioButton likesLowToHighButton;
    public RadioButton likesHighToLowButton;
    public ToggleGroup sortButtonsGroup;

    public Button mainPaneSearchPaneButton;
    public Text oopsyText;

    //JDBC - related member variables
    private Connection connection; //hold a reference to our connection and statement, this way all functions can use
    private Statement statement;   //them, saves overhead.

    //other variables
    private int MessageCount;
    private boolean searchPaneOpen = false;

    //we're going to use a dictionary to hold our messages, so accessing them by message ID is an O(1) operation
    //but java is Java so Dictionary is obsolete, we're using a HashMap
    //because Map<> is the immutable interface class
    //Java
    private final HashMap<Integer, ProcessedMessage> ProcessedMessages = new HashMap<>();

    @FXML
    public void initialize() throws SQLException {

        SetUpVisibleLists(mainMessageNodes, searchPaneNodes);

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

    public void SetUpVisibleLists(List<Node> main, List<Node> search) {
        main.addAll(List.of(
                buttonBox,
                messageBox,
                messageInputField,
                mainPaneSearchPaneButton));

        search.addAll(List.of(SearchPane));
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


    public void onVote(int MessageID, boolean upvote) {
        try {
            //get the ProcessedMessage and the attached buttons object
            ProcessedMessage pm = ProcessedMessages.get(MessageID);
            VoteButtons buttons = pm.getButtons();


            //ternary operator: decide which button we're using
            BoolButton toUse = upvote ? buttons.getUp() : buttons.getDown();

            //if its been pressed already, we want to do the opposite
            if (toUse.pressed) {
                //so tell it to -1 if its an upvote and +1 if its a downvote
                UpdateDatabaseAboutVote(MessageID, !upvote);
            } else {
                UpdateDatabaseAboutVote(MessageID, upvote);
            }

            //flip the boolean and change the colours
            toUse.pressed = !toUse.pressed;
            UpdateButtonColours(buttons);

            //update the texts on the PM
            UpdateVoteCountForMessage(MessageID, pm);

        } catch (Exception e) {
            System.err.println("done oopsy in upvote" + e);
        }
    }

    //performance improvements could be made here, we're excessively querying the DB
    public void UpdateVoteCountForMessage(int messageID, ProcessedMessage pm) throws SQLException {
        //prepare and make the sql statement
        String st = String.format("SELECT VoteSum FROM messages WHERE MessageId=%d;", messageID);
        ResultSet rs = statement.executeQuery(st);

        //process the ResultSet
        rs.first();
        int voteSum = rs.getInt(1);

        //tell the ProcessedMessage about it
        pm.UpdateVoteSum(voteSum);
    }

    public void UpdateButtonColours(VoteButtons buttons) {

        if (buttons.isUpPressed()) { //if the upvote button has been pressed, it should be orange.
            buttons.getUp().button.setStyle("-fx-background-color: orange");
        } else { //otherwise it should be grey
            buttons.getUp().button.setStyle(null);
        }

        if (buttons.isDownPressed()) { //if the downvote button is pressed, it should be blue
            buttons.getDown().button.setStyle("-fx-background-color: blue");
        } else { //otherwise, it should be grey
            buttons.getDown().button.setStyle(null);
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

    public void GenerateSearchSelectStatement() {

    }

    //make the search panel show up when we press the button
    public void onSearchPaneOpenButtonPressed() {

        //if it's false, set it to true
        searchPaneOpen = !searchPaneOpen;

        for (Node mainNode : mainMessageNodes) {
            mainNode.setVisible(!searchPaneOpen);
            mainNode.setManaged(!searchPaneOpen);
            //if the search pane is open, the main pane should not be open, hence not
        } //and vice versa
        for (Node searchNode : searchPaneNodes) {
            searchNode.setVisible(searchPaneOpen);
            searchNode.setManaged(searchPaneOpen);
        }

    }

    public void onSearchButtonPressed() throws SQLException {

        UpdateOopsyText(false);

        StringBuilder statementBuilder = new StringBuilder("SELECT * FROM messages\nWHERE");

        //get the pressed button
        String pressedButton = (String) sortButtonsGroup.getSelectedToggle().getUserData();

        //get the values from our fields
        String keyword = keywordInputField.getText();
        if (!keyword.equals("")) {                      //yes, i need this amount of % signs.
            statementBuilder.append(String.format(" Content LIKE \"%%%s%%\"\nAND", keyword));
        }

        //evaluate the date field
        LocalDate date1 = dateLowerBoundInputField.getValue(), date2 = dateUpperBoundInputField.getValue();

        //if at least one of them is not null. - adjusted using De Morgens
        if (!(date1 == null && date2 == null)) {
            if (date1 == null) {  //if the first field is empty, use second date as upper bound with no lower bound.
                statementBuilder.append(String.format(" TimeSent < \"%s 00:00:00\"\nAND", date2));
            } else if (date2 == null) { //if the second field is empty: this means the first one must be full due to above statement
                                        //so we will use the first date as a lower bound with no upper bound.
                statementBuilder.append(String.format(" TimeSent > \"%s 00:00:00\"\nAND", date1));
            } else { //they both have content so we will use the double bound thingy.
                if(date1.isAfter(date2)){
                    System.err.println("did oopsy in the validation thingy dates");
                    UpdateOopsyText(true);
                    return;
                }
                statementBuilder.append(String.format(" TimeSent BETWEEN \"%s 00:00:00\" AND \"%s 00:00:00\"\n AND", date1, date2));
            }
        }

        //consider the vote count - will be easier just to evaluate as strings, as ints are not nullable.

        String like1 = likesLowerBoundInputField.getText(), like2 = likesUpperBoundInputField.getText();

        //todo validation cos im too tired

        //if at least one of them is not null
        if ( !(like1.equals("") && like2.equals("")) ){
            if(like1.equals("")){
                statementBuilder.append(String.format(" VoteSum < %s\nAND", like2));
            } else if (like2.equals("")){
                statementBuilder.append(String.format(" VoteSum > %s\nAND", like1));
            } else {
                statementBuilder.append(String.format(" VoteSum BETWEEN %s AND %s\nAND", like1, like2));
            }
        }

        String author = userInputField.getText();
        //if it's not an empty string
        if(!author.equals("")){
            statementBuilder.append(String.format(" Author = \"%s\"\nAND", author));
        }

        //this is spaghetti code but it lets us get rid of that hanging AND
        statementBuilder.append(" 1=1\n");

        //append the sorting bit

        switch (pressedButton){
            case "likesHighToLowButton" -> statementBuilder.append("ORDER BY VoteSum DESC;");
            case "likesLowToHighButton" -> statementBuilder.append("ORDER BY VoteSum ASC;");
            case "timeSentOldestToNewest" -> statementBuilder.append("ORDER BY TimeSent DESC");
            case "timeSentNewestToOldest" -> statementBuilder.append("ORDER BY TimeSent ASC");
            default -> System.err.println("CHARLIE DID A BIG OOPSY");
        }

        String searchStatement = statementBuilder.toString();

        //lets send the search statement off to the database and see what happens:

        ResultSet rs = statement.executeQuery(searchStatement);

        //on a fresh result set, this will tell us whether the set is empty
        if(!rs.isBeforeFirst()){
            System.out.println("empty set!");
            return;
        }

        MessagesTable table = new MessagesTable(rs);

        HelperFunctions.PrintMessageTableNicely(table);
        System.out.println("\n--------------------------------------------\n");
    }

    public void UpdateOopsyText(boolean isVisible){
        oopsyText.setVisible(isVisible);
    }

}



