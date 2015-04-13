package com.cmpt276.meetly;

import com.cmpt276.meetly.IMeetlyServer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MeetlyServer implements IMeetlyServer {

    // This is horrible. Don't do this. Seriously.
    // For our purposes, it's just a time saver because we don't care too much
    // about robustness. Using a RESTful API is preferred in practice.
    private static final String url  =
            "jdbc:mysql://csil-messaging1.cs.surrey.sfu.ca/db1";
    private static final String user =
            "cmptuser";
    private static final String pass =
            "sUp3rS3cretp@ssw0rd";

    private java.sql.Connection getConnection()
            throws ClassNotFoundException, java.sql.SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }

    private final String login =
            "CALL db1.sp_LOGIN(?, ?, ?)";

    @Override
    public int login(String username, String password)
            throws FailedLoginException {
        try {
            Connection con = getConnection();
            CallableStatement loginStmt = con.prepareCall(login);
            loginStmt.setString(1, username);
            loginStmt.setString(2, password);
            loginStmt.registerOutParameter(3, Types.INTEGER);
            loginStmt.executeQuery();
            if (loginStmt.getInt(3) == 0) {
                throw new FailedLoginException("Error logging in");
            }
            return loginStmt.getInt(3);
        } catch (ClassNotFoundException cne) {
            throw new FailedLoginException(cne);
        } catch (java.sql.SQLException sqle) {
            throw new FailedLoginException(sqle);
        }
    }

    private final String create =
            "CALL db1.sp_CREATE_EVENT(?, ?, ?, ?, ?, ?, ?)";

    @Override
    public int publishEvent(String username, int userToken, String title,
                            Calendar startTime, Calendar endTime,
                            double latitude, double longitude)
            throws FailedPublicationException {
        try {
            Connection con = getConnection();
            CallableStatement createStmt = con.prepareCall(create);
            createStmt.setInt(1, userToken);
            createStmt.setString(2, title);
            createStmt.setLong(3, startTime.getTimeInMillis());
            createStmt.setLong(4, endTime.getTimeInMillis());
            createStmt.setDouble(5, latitude);
            createStmt.setDouble(6, longitude);
            createStmt.registerOutParameter(7, Types.INTEGER);
            createStmt.executeQuery();
            if (createStmt.getInt(7) == 0) {
                throw new FailedPublicationException("Error creating event");
            }
            return createStmt.getInt(7);
        } catch (ClassNotFoundException cne) {
            throw new FailedPublicationException(cne);
        } catch (java.sql.SQLException sqle) {
            throw new FailedPublicationException(sqle);
        }
    }

    private final String modify =
            "CALL db1.sp_MODIFY_EVENT(?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public void modifyEvent(int eventID, int userToken, String title,
                            Calendar startTime, Calendar endTime,
                            double latitude, double longitude)
            throws FailedPublicationException {
        try {
            Connection con = getConnection();
            CallableStatement modifyStmt = con.prepareCall(modify);
            modifyStmt.setInt(1, eventID);
            modifyStmt.setInt(2, userToken);
            modifyStmt.setString(3, title);
            modifyStmt.setLong(4, startTime.getTimeInMillis());
            modifyStmt.setLong(5, endTime.getTimeInMillis());
            modifyStmt.setDouble(6, latitude);
            modifyStmt.setDouble(7, longitude);
            modifyStmt.registerOutParameter(8, Types.INTEGER);
            modifyStmt.executeQuery();
            if (modifyStmt.getInt(8) != 1) {
                throw new FailedPublicationException("Error updating event");
            }
        } catch (ClassNotFoundException cne) {
            throw new FailedPublicationException(cne);
        } catch (java.sql.SQLException sqle) {
            throw new FailedPublicationException(sqle);
        }
    }

    private final String fetch =
            "CALL db1.sp_FETCH_EVENTS(?)";

    @Override
    public List<MeetlyEvent> fetchEventsAfter(int lastTick)
            throws FailedFetchException {
        try {
            Connection con = getConnection();
            CallableStatement fetchStmt = con.prepareCall(fetch);
            fetchStmt.setInt(1, lastTick);
            ResultSet results = fetchStmt.executeQuery();

            ArrayList<MeetlyEvent> events = new ArrayList<MeetlyEvent>();
            ResultSetMetaData meta = results.getMetaData();
            int colCount = meta.getColumnCount();
            while (results.next()) {
                MeetlyEvent event = new MeetlyEvent();
                event.eventID    = results.getInt(1);
                event.lastUpdate = results.getInt(2);
                event.title      = results.getString(4);

                event.startTime  = Calendar.getInstance();
                event.startTime.setTimeInMillis(results.getLong(5));
                event.endTime    = Calendar.getInstance();
                event.endTime.setTimeInMillis(results.getLong(6));

                event.latitude   = results.getDouble(7);
                event.longitude  = results.getDouble(8);
                events.add(event);
            }
            return events;
        } catch (ClassNotFoundException cne) {
            throw new FailedFetchException(cne);
        } catch (java.sql.SQLException sqle) {
            throw new FailedFetchException(sqle);
        }
    }
}