package com.example.discord2test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Discord2Loader extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Discord2Loader.class.getResource("loginScreen.fxml")); //loads in sheet
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080); //create a new seen with height and width
        stage.setTitle("Discord 2"); //set title
        stage.setScene(scene);    //set the scene on stage
        stage.setMaximized(true);
        stage.show();             //open the curtains
    }

    public static void main(String[] args) { launch();}
}