package com.example.discord2test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Scanner;

public class Listener extends Thread {

    private Socket socket;

    private Scanner in;
    private PrintWriter out;

    private boolean url = false, user = false, pwd = false;

    private final Discord2Controller parentController;

    public Listener(String ip, int port, Discord2Controller controller) {
        this.parentController = controller;
        try {
            //initalise the socket and the output streams
            socket = new Socket(ip, port);
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
        while (!socket.isClosed() && !currentThread().isInterrupted()) {
            String comingIn = in.nextLine();
            if (ISUCC.CLIENT_CHECK_MESSAGES.equals(comingIn)) {
                //instruct the main thread to check for new messages
                System.out.println("check for new messages!");

                try {
                    parentController.GetNewMessages();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else {
                //the object could be our username, password or or url thing
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
                    default -> System.err.println("unrecognised message");
                }
            }
        }
    }
}
