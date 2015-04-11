package com.cmpt276.meetly;

import java.util.Calendar;

public interface IMeetlyServer {
    /**
     * Logs a user into the central server. This method takes a username and
     * a password. If the user already exists in the data base then:
     *   1) If the password is correct for the user, the user is logged in
     *     and a token for the user is returned.
     *   2) If the password is incorrect for the user, a FailedLoginException is
     *     thrown.
     * If the user does not already exist in the database, then the user is
     * created with the given password, and the a token for the user is
     * returned.
     * @param username
     * @param password
     * @return integer token for the user
     * @throws FailedLoginException
     */
    public int login(String username, String password)
            throws FailedLoginException;

    /**
     * Publishes an event to the central server. Takes all relevant data for an
     * event and publishes that event on the central server. If the publication
     * fails for any reason, a FailedPublicationException is thrown. If the
     * event is successfully published, a unique ID for the event is returned.
     * @param username
     * @param userToken
     * @param title
     * @param startTime
     * @param endTime
     * @param latitude
     * @param longitude
     * @return
     * @throws FailedPublicationException
     */
    public int publishEvent(String username, int userToken,
                            String title, Calendar startTime, Calendar endTime,
                            double latitude, double longitude)
            throws FailedPublicationException;

    /**
     * Modifies an existing event. Given the relevant details for an event,
     * modifies the existing event on the central server. If publication of the
     * modified event fails for any reason, a FailedPublicationException is
     * thrown. If the event is successfully published, the method terminates
     * normally.
     * @param eventID
     * @param userToken
     * @param title
     * @param startTime
     * @param endTime
     * @param latitude
     * @param longitude
     * @throws FailedPublicationException
     */
    public void modifyEvent(int eventID, int userToken,
                            String title, Calendar startTime, Calendar endTime,
                            double latitude, double longitude)
            throws FailedPublicationException;

    /**
     * Retrieves all events after the given update tick
     * @param lastTick
     * @return A list of events
     * @throws FailedFetchException
     */
    public java.util.List<MeetlyEvent> fetchEventsAfter(int lastTick)
            throws FailedFetchException;

    class FailedLoginException extends Exception {
        public FailedLoginException(Exception e) {
            super(e);
        }
        public FailedLoginException(String message) {
            super(message);
        }
    }

    class FailedPublicationException extends Exception {
        public FailedPublicationException(Exception e) {
            super(e);
        }
        public FailedPublicationException(String message) {
            super(message);
        }
    }

    class FailedFetchException extends Exception {
        public FailedFetchException(Exception e) {
            super(e);
        }
    }

    class MeetlyEvent {
        public int eventID;
        public int lastUpdate;
        public String title;
        public Calendar startTime;
        public Calendar endTime;
        public double latitude;
        public double longitude;
    }
}
