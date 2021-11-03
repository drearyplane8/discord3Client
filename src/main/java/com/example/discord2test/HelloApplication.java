package com.example.discord2test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml")); //loads in sheet
        Scene scene = new Scene(fxmlLoader.load(), 320, 240); //create a new seen with height and width
        stage.setTitle("Hello!"); //set title
        stage.setScene(scene);    //set the scene on stage
        stage.show();             //open the curtains
    }

    public static void main(String[] args) {
        launch();
    }
}