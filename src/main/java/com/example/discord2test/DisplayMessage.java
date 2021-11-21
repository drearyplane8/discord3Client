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
public class DisplayMessage {

    private int MessageID;

    private Text author, text, date;

    private final HBox VoteArrows = new HBox();

    private static final double BUTTON_HEIGHT = 25, BUTTON_WIDTH = 25;

    public DisplayMessage(String author, String text, Instant date, Discord2Controller controller, int MessageID) {

        this.MessageID = MessageID;

        this.author = new Text(author);
        this.text = new Text(text);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        String formatted = formatter.format(date);

        this.date = new Text(formatted);

        Button up = new Button("^");
        up.setOnAction(actionEvent -> controller.onUpvote(MessageID)); //set up upvote and downvote arrows linked to our
        SetUpButton(up);

        Button down = new Button("v");
        down.setOnAction(actionEvent -> controller.onDownvote(MessageID));
        SetUpButton(down);

        VoteArrows.getChildren().addAll(up, down); //add our arrows to their vbox so they stand up on top of each other

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
        return new HBox(8, author, text, date, VoteArrows);

    }

    public void SetUpButton(Button button){

        button.setMinHeight(BUTTON_HEIGHT);
        button.setMaxHeight(BUTTON_HEIGHT);

        button.setMinWidth(BUTTON_WIDTH);
        button.setMaxWidth(BUTTON_WIDTH);

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