package com.example.discord2test;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    private VoteButtons buttons; //buttons is a new holder class for some stuff to tidy up this class: its too damn big


    public ProcessedMessage(String author, String text, Instant date, Discord2Controller controller, int MessageID) {

        buttons = new VoteButtons(controller, this); //set up our new buttons class?

        this.MessageID = MessageID;

        this.author = new Text(author);
        this.text = new Text(text);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        String formatted = formatter.format(date);

        this.date = new Text(formatted);


    }

    public int getMessageID() {
        return MessageID;
    }

    public void setMessageID(int messageID) {
        MessageID = messageID;
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

    }

    /**
     *
     * @param arrow a string. put in "up" for upvote arrow and "down" for downvote arrow
     * @return the called for button
     */
    public Button GetVoteArrow(String arrow){
        if(arrow.equals("up")) return buttons.getUp();
        else if(arrow.equals("down")) return buttons.getDown();
        else throw new IllegalArgumentException("you put in the wrong argument to GetVoteArrow");
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

    public VoteButtons getButtons() {
        return buttons;
    }
}

class colours {

    public static Color
            authorIsMe = Color.RED,
            authorIsSomeoneElse = Color.BLUE,
            content = Color.BLACK,
            date = Color.color(0.5f, 0.5f, 0.5f);
}