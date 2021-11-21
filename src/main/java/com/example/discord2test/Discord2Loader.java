package com.example.discord2test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Discord2Loader extends Application {


    private Stage stage;

    @Override
    public void start(Stage stage) throws IOException {

        Globals.loader = this;

        this.stage = stage; //set the member stage to be the same as the stage in this application.

        FXMLLoader fxmlLoader = new FXMLLoader(Discord2Loader.class.getResource("loginScreen.fxml")); //loads in sheet
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080); //create a new seen with height and width
        stage.setTitle("Discord 2"); //set title

        stage.setScene(scene);    //set the scene on stage
        stage.setMaximized(true);
        stage.show();             //open the curtains
    }


    public void changeScene(String fxml) throws IOException  {
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        stage.getScene().setRoot(pane);
    }


    public static void main(String[] args) { launch();}
}