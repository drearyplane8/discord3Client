package com.example.discord2test;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class HelperFunctions {

    public static String GetDatabaseVersion(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return String.format("JDBC version %d.%d\n", metaData.getJDBCMajorVersion(), metaData.getJDBCMinorVersion());
    }

    /***
     *
     * @param row the message row to print nicely
     * @param authormsg a boolean value to say whether this message should be displayed as an author message or not
     */
    public static void PrintMessageNicely(MessagesRow row, boolean authormsg) {

        final Attribute nameColour = authormsg ? Attribute.RED_TEXT() : Attribute.BLUE_TEXT();
        final Attribute DATE_GREY = Attribute.TEXT_COLOR(128, 128, 128);

        System.out.printf("%s: %s    | %s\n", Ansi.colorize(row.Author, nameColour), row.Text, Ansi.colorize(row.TimeSent.toString(), DATE_GREY));
    }

    public static String StatementToNiceString(String SQL, Statement statement) throws SQLException {
        return new MessagesTable(statement.executeQuery(SQL)).toNiceString();
    }

    /***
     *
     * @param statement the SQL statement object to work with
     * @return the number of rows in the messages table
     * @throws SQLException because intellij made me
     */
    public static int GetMessageCount(Statement statement) throws SQLException {
        ResultSet CountRS = statement.executeQuery("SELECT count(*) FROM messages"); //get the amount of messages
        CountRS.first();                     //since result is returned as a 1x1 table, look at the first and only row
        return CountRS.getInt(1); //and the first and only column

    }

}
