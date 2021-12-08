package com.example.discord2test;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;


public class VoteButtons {

    private final HBox box = new HBox();

    private final BoolButton up;
    private final BoolButton down;

    private static final double BUTTON_HEIGHT = 25, BUTTON_WIDTH = 25;

    public VoteButtons(Discord2Controller controller, ProcessedMessage owner) {

        Button upButton = new Button("^");
        upButton.setOnAction(actionEvent -> controller.onVote(owner.getMessageID(), true)); //set up upvote and downvote arrows linked to our PM class
        SetUpButton(upButton);
        up = new BoolButton(upButton);

        Button downButton = new Button("v");
        downButton.setOnAction(actionEvent -> controller.onVote(owner.getMessageID(), false));
        SetUpButton(downButton);
        down = new BoolButton(downButton);

        //add them to the cheeky hbox
        box.getChildren().addAll(up.button, down.button);

    }

    public void SetUpButton(Button button){

        button.setMinHeight(BUTTON_HEIGHT);
        button.setMaxHeight(BUTTON_HEIGHT);

        button.setMinWidth(BUTTON_WIDTH);
        button.setMaxWidth(BUTTON_WIDTH);

    }

    public void setUpPressed(boolean upPressed) {
        this.up.pressed = upPressed;
    }
    public void setDownPressed(boolean downPressed) { this.down.pressed = downPressed;}

    public HBox getBox() {
        return box;
    }
    public BoolButton getUp() {
        return up;
    }
    public BoolButton getDown() {
        return down;
    }

    public boolean isUpPressed() {
        return up.pressed;
    }
    public boolean isDownPressed() {
        return down.pressed;
    }
}


