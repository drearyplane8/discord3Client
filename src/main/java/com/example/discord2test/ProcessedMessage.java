package com.example.discord2test;


import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/***
 * A class that exists to take the data from a MessageRow and put it into a nice javafx format.
 */
public class ProcessedMessage {

    private int MessageID;
    private Text author, text, date;
    private final VoteButtons buttons; //buttons is a new holder class for some stuff to tidy up this class: its too damn big


    public ProcessedMessage(String author, String text, Instant date, Discord2Controller controller, int MessageID) {

        buttons = new VoteButtons(controller, this); //set up our new buttons class

        this.MessageID = MessageID;

        this.author = new Text(author);
        this.text = new Text(text);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        String formatted = formatter.format(date);

        this.date = new Text(formatted);
    }

    //an alternate constructor which takes a message row, kinda makes more sense this way
    public ProcessedMessage(MessagesRow row, Discord2Controller controller){
        buttons = new VoteButtons(controller, this); //set up our new buttons class

        this.MessageID = row.MessageID;

        this.author = new Text(row.Author);
        this.text = new Text(row.Text);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        String formatted = formatter.format(row.TimeSent);

        this.date = new Text(formatted);
    }

    public int getMessageID() {
        return MessageID;
    }

    public VoteButtons getButtons() {
        return buttons;
    }

    public void setColours(boolean userMsg) {

        Color authorColour = userMsg ? colours.authorIsMe : colours.authorIsSomeoneElse;

        author.setFill(authorColour);
        text.setFill(colours.content);
        date.setFill(colours.date);

    }

    public HBox GetHbox() { //returns a HBOX with the three texts in it - javafx shits the bed if you try add duplicate kids
                            //and the vote arrows
        return new HBox(8, author, text, date, buttons.getBox());
    } //this class should only be called once! if charlie ever has an error because he thinks
    //this method returns a stored Hbox, tell him he's stupid.

    //currently these getters and setters are useless, will keep them here until release so testing.
    public void setMessageID(int messageID) {
        MessageID = messageID;
    }
    public Text getAuthor() {
        return author;
    }
    public void setAuthor(Text author) {
        this.author = author;
    }
    public Text getText() {
        return text;
    }
    public void setText(Text text) {
        this.text = text;
    }
    public Text getDate() {
        return date;
    }
    public void setDate(Text date) {
        this.date = date;
    }


}

class colours {

    public static Color
            authorIsMe = Color.RED,
            authorIsSomeoneElse = Color.BLUE,
            content = Color.BLACK,
            date = Color.color(0.5f, 0.5f, 0.5f);
}