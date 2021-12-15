package com.example.discord2test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;

public class MessagesRow {

    public int MessageID;
    String Author;
    String Text;
    Instant TimeSent;
    int VoteSum;


    String FileExtension;
    InputStream FileData;

    /**
     * @param row the index of the row of the ResultSet that we're converting into a MessagesRow
     * @param rs  the ResultSet that we're reading off
     * @throws SQLException if any of the SQL stuff fails
     */
    public MessagesRow(int row, ResultSet rs) throws SQLException {
        rs.absolute(row); //make sure we are  looking at the right row
        MessageID = rs.getInt(1);
        Author = rs.getString(2);
        Text = rs.getString(3);

        TimeSent = rs.getTimestamp(4).toInstant(); //put it in the swanky new Instant class
        VoteSum = rs.getInt(5);

        FileExtension = rs.getString(6);
        FileData = rs.getBinaryStream(7); //we probably wanna replace this with JDBC.Blob
    }

    public MessagesRow(String author, String text, Instant timeSent, String fileExtension, InputStream fileData) {
        Author = author;
        Text = text;
        TimeSent = timeSent;
        VoteSum = 0;
        FileExtension = fileExtension;
        FileData = fileData;
    }


    public static String InstantToMySQLFormat(Instant instant) { //convert an Instant object to the string representation mysql wants
        return new Timestamp(instant.toEpochMilli()).toString();
    }

    @Override
    public String toString() {
        return "MessagesRow{" +
                "MessageID=" + MessageID +
                ", Author='" + Author + '\'' +
                ", Text='" + Text + '\'' +
                ", TimeSent=" + TimeSent +
                ", VoteSum=" + VoteSum +
                ", FileExtension='" + FileExtension + '\'' +
                ", FileData=" + FileData +
                '}';
    }

    public PreparedStatement CreatePreparedInsertStatement(String tableName, Connection con) throws SQLException {
        String statement = String.format("INSERT INTO %s\n", tableName) +
                "Values(default, ?, ?, ?, ?, ?, ?);";

        PreparedStatement ps = con.prepareStatement(statement);

        //mysql everything starts at 1!
        //except it starts at 2 because the first field is default? i don't actually know.
        ps.setString(1, Author);
        ps.setString(2, Text);
        ps.setString(3, InstantToMySQLFormat(TimeSent));
        ps.setInt(4, VoteSum);

        //for file extension and file data, just send the null string until we make this work.
        ps.setString(5, FileExtension);
        ps.setBinaryStream(6, FileData);

        return ps;
    }

    public String toNiceString(){
        return String.format("%s: %s  (%s)", Author, Text, TimeSent);
    }


}
