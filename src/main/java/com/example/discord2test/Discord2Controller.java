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
import javafx.stage.FileChooser;
import jdk.jfr.Description;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Discord2Controller {

    //we're going to use a dictionary to hold our messages, so accessing them by message ID is an O(1) operation
    //but java is Java so Dictionary is obsolete, we're using a HashMap
    //because Map<> is the immutable interface class
    //Java
    private final HashMap<Integer, ProcessedMessage> ProcessedMessages = new HashMap<>();
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

    public ToggleGroup sortButtonsGroup;
    public Button mainPaneSearchPaneButton;
    public Text oopsyText;
    public VBox searchMessageBox;
    public Text fileNameText;
    public CheckBox sendFileCheckBox;

    //JDBC - related member variables
    private Connection connection; //hold a reference to our connection and statement, this way all functions can use
    private Statement statement;   //them, saves overhead.

    //other variables
    private int MessageCount;
    private boolean searchPaneOpen = false;

    //hold the file selected by the file selector
    //this can be null!
    private File currentlySelectedFile;

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

        search.addAll(List.of(SearchPane, searchMessageBox));
    }

    //09/12 refactoring this code to use PreparedStatement
    public void onSubmit() throws SQLException {

        FileInputStream fileToSendAsStream = null;
        String fileToSendExtension = null;

        //if a file is currently selected, and the checkbox to send the file is clicked
        if(currentlySelectedFile != null && sendFileCheckBox.isSelected()){
            try{
                fileToSendAsStream = new FileInputStream(currentlySelectedFile);
                //to get the extension, split the filename on the '.' character and take the latter part.
                fileToSendExtension = currentlySelectedFile.getName().split("\\.")[1];
            } catch (Exception e) {
                System.err.println("Done fucky wucky in the onSubmit file handling " + e);
            }
        }

        MessagesRow mr = new MessagesRow(Globals.username, messageInputField.getText(),
                Instant.now(), fileToSendExtension, fileToSendAsStream);

        //pass our member reference to the connection.
        PreparedStatement ps = mr.CreatePreparedInsertStatement("messages", connection);
        int result = ps.executeUpdate();

        System.out.printf("Sent message insert statement to the DB, result code %d\n", result);


        //this is a good time to refresh the message list, since we have no way of client communication yet
        GetNewMessages();
        MessageCount++;
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

        //firstly, see if we're upvoting or downvoting.it
        int voteToAdd;
        if (positive) voteToAdd = 1;
        else voteToAdd = -1;

        //generate an update statement
        final String updateStatement = String.format("UPDATE messages SET VoteSum = VoteSum + %d WHERE MessageID = %d", voteToAdd, MessageID);

        statement.executeUpdate(updateStatement);
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

    //this is an absolute monster of a function.
    public void onSearchButtonPressed() throws SQLException {

        //clear the error text initially
        UpdateOopsyText("", false);

        //start a StringBuilder with what'll eventually be our search statement
        StringBuilder statementBuilder = new StringBuilder("SELECT * FROM messages\nWHERE");

        //get the pressed button's UserData so we know which one we've pressed
        String pressedButton = (String) sortButtonsGroup.getSelectedToggle().getUserData();

        //get the values from our keyword field
        String keyword = keywordInputField.getText();
        if (!keyword.equals("")) {                      //yes, i need this amount of % signs.
            statementBuilder.append(String.format(" Content LIKE \"%%%s%%\"\nAND", keyword));
        }

        //evaluate the date field
        LocalDate date1 = dateLowerBoundInputField.getValue(), date2 = dateUpperBoundInputField.getValue();

        //if at least one of them is not null. - adjusted using De Morgen's
        if (!(date1 == null && date2 == null)) {
            try {
                statementBuilder.append(GetDateComponentOfSearchStatement(date1, date2));
            } catch (UnsupportedOperationException e) {
                System.err.println("Date range is wrong.");
                UpdateOopsyText("Error: Invalid date range (are your dates the wrong way round?)", true);
                return; //cancel execution
            }
        }

        //consider the vote count - will be easier just to evaluate as strings, as ints are not nullable.

        String like1 = likesLowerBoundInputField.getText(), like2 = likesUpperBoundInputField.getText();
        try {
            //no need to run this if theyre both null, we're not trying to search in that case.
            if (!(like1.equals("") && like2.equals(""))) {
                statementBuilder.append(GetLikeComponentOfSearchStatement(like1, like2));
            }
        } catch (NumberFormatException e) {
            System.err.println("one parameter wasnt an int");
            UpdateOopsyText("Error: Invalid values for the votes field. Make sure they contain only 0-9 and the minus '-' sign", true);
            return; //cancel execution
        } catch (UnsupportedOperationException e) {
            System.err.println("one was greater than the other");
            UpdateOopsyText("Error: Invalid vote range. Make sure the votes are in the right order.", true);
            return; //cancel execution
        }

        String author = userInputField.getText();
        //if it's not an empty string
        if (!author.equals("")) {
            statementBuilder.append(String.format(" Author = \"%s\"\nAND", author));
        }

        //this is spaghetti code but it lets us get rid of that hanging AND
        statementBuilder.append(" 1=1\n");

        //append the sorting bit

        switch (pressedButton) {
            case "likesHighToLowButton" -> statementBuilder.append("ORDER BY VoteSum DESC;");
            case "likesLowToHighButton" -> statementBuilder.append("ORDER BY VoteSum ASC;");
            case "timeSentOldestToNewest" -> statementBuilder.append("ORDER BY TimeSent ASC;");
            case "timeSentNewestToOldest" -> statementBuilder.append("ORDER BY TimeSent DESC;");
            default -> System.err.println("CHARLIE DID A BIG OOPSY");
        }

        String searchStatement = statementBuilder.toString();
        System.out.println("Final SQL Statement:\n" + searchStatement);
        //lets send the search statement off to the database and see what happens:

        ResultSet rs = statement.executeQuery(searchStatement);

        //on a fresh result set, this will tell us whether the set is empty
        if (!rs.isBeforeFirst()) {
            //clear the search message box
            searchMessageBox.getChildren().clear();

            //display the text that no messages found.
            searchMessageBox.getChildren().add(new Text("No results found."));
            return;
        }

        MessagesTable table = new MessagesTable(rs);
        DisplayMessageTableInVBox(table, searchMessageBox, true);

    }

    public void UpdateOopsyText(String contents, boolean isVisible) {
        oopsyText.setText(contents);
        oopsyText.setVisible(isVisible);
    }

    public String GetDateComponentOfSearchStatement(LocalDate date1, LocalDate date2) throws UnsupportedOperationException {
        StringBuilder builder = new StringBuilder();

        if (date1 == null) {  //if the first field is empty, use second date as upper bound with no lower bound.
            //Hard less than midnight the next day will return any messages sent on the day input, to be consistent
            //with the BETWEEN operator.
            builder.append(String.format(" TimeSent < \"%s 00:00:00\"\nAND", HelperFunctions.nextDay(date2)));

        } else if (date2 == null) { //if the second field is empty: this means the first one must be full due to above statement
            //so we will use the first date as a lower bound with no upper bound.
            //this one is fine: it will include any messages sent on the included day.
            builder.append(String.format(" TimeSent >= \"%s 00:00:00\"\nAND", date1));

        } else { //they both have content so we will use the double bound thingy.
            if (date1.isAfter(date2)) {
                throw new UnsupportedOperationException("Invalid date range.");
            }
            builder.append(String.format(" TimeSent BETWEEN \"%s 00:00:00\" AND \"%s 00:00:00\"\n AND", date1, date2));
        }
        return builder.toString();
    }

    public String GetLikeComponentOfSearchStatement(String like1, String like2)
            throws NumberFormatException, UnsupportedOperationException {

        int like1int, like2int;

        //if we got here, at least one of them is not null.

        if (!like1.equals("")) {
            like1int = Integer.parseInt(like1);
            //if like1 isnt null and like2 is, we're doing lower bound.
            if (like2.equals("")) {
                return String.format(" VoteSum >= %s\nAND", like1);
            } else {
                //ok so at this point we're doing BETWEEN
                like2int = Integer.parseInt(like2);
                if (like1int > like2int) {
                    //our two parameters are in the wrong order.
                    throw new UnsupportedOperationException("Vote things out of order.");
                }
                return String.format(" VoteSum BETWEEN %s AND %s\nAND", like1, like2);
            }
        } else { //if like1 is "", then that means we must be doing like2 only, therefore less than.
            //yes the result is ignored: we're using it as a validation only.
            Integer.parseInt(like2);
            return String.format(" VoteSum <= %s\nAND", like2);
        }

    }

    //is this scalable? no but itll do
    public void onClearSearchFiltersButtonPressed() {

        keywordInputField.setText("");
        userInputField.setText("");
        likesLowerBoundInputField.setText("");
        likesUpperBoundInputField.setText("");

        dateLowerBoundInputField.setValue(null);
        dateUpperBoundInputField.setValue(null);

    }

    public void onFileChooserButtonPressed() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select File");
        currentlySelectedFile = chooser.showOpenDialog(Globals.stage);
        UpdateFileNameText();
    }

    public void UpdateFileNameText(){
        if (currentlySelectedFile == null){
            fileNameText.setText("No file selected.");
        } else {
            fileNameText.setText(currentlySelectedFile.getName());
        }
    }

    public void onClearFileButtonPressed() {
        currentlySelectedFile = null;
        UpdateFileNameText();
    }

}



