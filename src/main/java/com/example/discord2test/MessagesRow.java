package com.example.discord2test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) { //stolen beautifully off the internet.
        byte[] hexChars = new byte[bytes.length * 2]; //converts a byte array to a hexadecimal string using bitwise magic
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static String InstantToMySQLFormat(Instant instant) { //convert an Instant object to the string representation mysql wants
        return new Timestamp(instant.toEpochMilli()).toString();
    }

    /**
     * @param tableName the name of a table to generate an insert statement for
     * @return a correctly formatted insert statement using the data from this MessagesRow
     *//// @throws IOException if converting from the FileStream to a hexadecimal string fails.

    public String CreateInsertStatement(String tableName) {

        /*String hexString;
        if (FileData != null) {
            byte[] FileBytes = FileData.readAllBytes();
            hexString = bytesToHex(FileBytes);
        } else hexString = "NULL";
*/
        return String.format("INSERT INTO %s\n", tableName) +
                String.format("Values(default, \"%s\", \"%s\", \"%s\", %d, \"%s\", \"%s\");", Author, Text, InstantToMySQLFormat(TimeSent), VoteSum, FileExtension, null);
    } //todo format the date better

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

    public String toNiceString(){
        return String.format("%s: %s  (%s)", Author, Text, TimeSent);
    }


}
