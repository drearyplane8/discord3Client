package com.example.discord2test;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Globals {

    public static final String URL = "jdbc:mysql://localhost:3306/discord2db";
    public final static String USERNAME = "java";
    final public static String PASSWORD = "java";

    //a hashset of all supported image formats.
    public static final HashSet<String> ImageExtensions = new HashSet<>(
            List.of("png", "jpg", "jpeg", "bmp", "gif")
    );

    static public String username;
    public static Discord2Loader loader;
    static public Stage stage;

}
