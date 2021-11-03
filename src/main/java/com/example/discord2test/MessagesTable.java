package com.example.discord2test;

import jdk.jfr.Description;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Description("Please don't put an entire large database into one of these!")
public class MessagesTable {

    private final List<MessagesRow> rows = new ArrayList<>();

    /**
     * @param rs the result set we're reading into this class
     * @throws SQLException if any operations on the ResultSet fail
     */
    public MessagesTable(ResultSet rs) throws SQLException {

        rs.first(); //make sure cursor in on first row
        int currentRow = 1;

        while (!rs.isAfterLast()) {
            rows.add(
                    new MessagesRow(currentRow, rs)
            );
            rs.next();
            currentRow++;
        }
    }

    public MessagesTable(ResultSet rs, int start) throws SQLException{
        rs.absolute(start); //make sure cursor in on first row
        int currentRow = 1;

        while (!rs.isAfterLast()) {
            rows.add(
                    new MessagesRow(currentRow, rs)
            );
            rs.next();
            currentRow++;
        }
    }

    /**
     * @param index the index of the row we want
     * @return a MessagesRow object
     */
    public MessagesRow getMessageRow(int index) {
        return rows.get(index);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("MessagesTable " + this.hashCode() + ":\n");

        for (MessagesRow row : rows) {
            out.append(row.toString()).append("\n");
        }
        return out.toString();
    }

    public String toNiceString() {
        StringBuilder out = new StringBuilder("");

        for (MessagesRow row : rows) {
            out.append(row.toNiceString()).append("\n");
        }
        return out.toString();
    }

    public List<MessagesRow> getRows() {
        return rows;
    }
}
