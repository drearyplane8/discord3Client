package com.example.discord2test;

import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

public class Listener extends Thread {

    private final Discord2Controller parentController;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    private volatile boolean url = false, user = false, pwd = false;

    public Listener(Socket socket, Discord2Controller controller) {
        this.parentController = controller;
        try {
            this.socket = socket;
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void AskForDBCreds() {
        System.out.println("asking for DB credentials");
        out.println(ISUCC.CRED_REQUEST);
    }

    public void TellServerWeSentAMessage() {
        out.println(ISUCC.CLIENT_MESSAGE_SENT);
    }

    public synchronized boolean AreAllTheCredsIn() {
        return (url && user && pwd);
    }

    @Override
    public void run() {
        System.out.println("run running");
        while (!socket.isClosed() && !currentThread().isInterrupted()) {
            System.out.println("in while loop");
            String comingIn = in.nextLine();
            if (ISUCC.CLIENT_CHECK_MESSAGES.equals(comingIn)) {

                //instruct the main thread to check for new messages
                System.out.println("check for new messages!");

                //what do the two brackets do? no idea. but this is neater than the anonymous class syntax.
                //this runnable tells the parent controller to check new messages.
                Runnable GetNewMessages = () -> {
                    try {
                        parentController.GetNewMessages();
                    } catch (SQLException e) {
                        parentController.ShowErrorBox("Discord2 Error",
                                "An error occurred fetching new messages",
                                "An error occurred interfacing with the database to fetch new messages.");
                    }
                };
                //tell JavaFX to slap our new runnable somewhere on the event queue
                Platform.runLater(GetNewMessages);

            } else {
                //the object could be our username, password or url thing
                String[] splitComingIn = comingIn.split("\"");

                switch (splitComingIn[0]) {
                    case "URL" -> {
                        Globals.URL = splitComingIn[1];
                        url = true;
                    }
                    case "UN" -> {
                        Globals.DB_USERNAME = splitComingIn[1];
                        user = true;
                    }
                    case "PWD" -> {
                        Globals.PASSWORD = splitComingIn[1];
                        pwd = true;
                    }
                    default -> {
                        parentController.ShowErrorBox("Discord2 Security Error",
                                "An unexpected message has been received from the server.",
                                "This could mean your connection is not secure. Please contact the server" +
                                        "administrator.");
                    }
                }
            }
        }
    }
}
