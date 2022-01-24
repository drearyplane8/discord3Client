package com.example.discord2test;

import javafx.stage.Stage;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Globals {

    public static String URL;
    public static String DB_USERNAME;
    public static String PASSWORD;

    public static String IP;
    public static int port;

    //a hashset of all supported image formats.
    public static final HashSet<String> ImageExtensions = new HashSet<>(
            List.of("png", "jpg", "jpeg", "bmp", "gif")
    );

    public static Socket socket;

    static public String username;
    public static Discord2Loader loader;
    static public Stage stage;

}
