package com.example.discord2test;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/***
 * A class that exists to take the data from a MessageRow and put it into a nice javafx format.
 */
public class DisplayMessage {

    private Text author, text, date;

    public DisplayMessage(String author, String text, Instant date) {

        this.author = new Text(author);
        this.text = new Text(text);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
        String formatted = formatter.format(date);

        this.date = new Text(formatted);
    }

    public HBox GetHbox(){ //returns a HBOX with the three texts in it - javafx shits the bed if you try add duplicate kids

        return new HBox(8, author, text, date);

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
