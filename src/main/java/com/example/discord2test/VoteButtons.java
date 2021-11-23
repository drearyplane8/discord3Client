package com.example.discord2test;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


public class VoteButtons {

    private HBox box;

    private Button up;
    private Button down;

    private boolean upPressed;
    private boolean downPressed;

    private static final double BUTTON_HEIGHT = 25, BUTTON_WIDTH = 25;

    public VoteButtons(Discord2Controller controller, ProcessedMessage owner) {
        Button up = new Button("^");
        up.setOnAction(actionEvent -> controller.onUpvote(owner.getMessageID())); //set up upvote and downvote arrows linked to our PM class
        SetUpButton(up);

        Button down = new Button("v");
        down.setOnAction(actionEvent -> controller.onDownvote(owner.getMessageID()));
        SetUpButton(down);

        //add them to the cheeky hbox
        box.getChildren().addAll(up, down);

    }

    public void SetUpButton(Button button){

        button.setMinHeight(BUTTON_HEIGHT);
        button.setMaxHeight(BUTTON_HEIGHT);

        button.setMinWidth(BUTTON_WIDTH);
        button.setMaxWidth(BUTTON_WIDTH);

    }

    public void setUpPressed(boolean upPressed) {
        this.upPressed = upPressed;
    }
    public void setDownPressed(boolean downPressed) {
        this.downPressed = downPressed;
    }

    public HBox getBox() {
        return box;
    }
    public Button getUp() {
        return up;
    }
    public Button getDown() {
        return down;
    }
    public boolean isUpPressed() {
        return upPressed;
    }
    public boolean isDownPressed() {
        return downPressed;
    }
}
