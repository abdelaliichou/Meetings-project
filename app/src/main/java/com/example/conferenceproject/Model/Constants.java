package com.example.conferenceproject.Model;

import java.util.HashMap;

public class Constants {
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static String REMOTE_MSG_TYPE = "type";
    public static String REMOTE_MSG_INVITATION = "invitation";
    public static String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static String REMOTE_INVITER_TOKEN = "inviterToken";
    public static String REMOTE_MSG_DATA = "data";
    public static String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static String REMOTE_MSG_MEETING_ROOM = "MeetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAGZj4gGo:APA91bGkZZ124U9ng5RSrnaQ22REXprqRPpdZsEdslOwHOl3JConigyQmdAYLYgvXp2C9CsrAtIPs3LxzoZ3HSglL0qNmqTYisIIJgYCKElARTgfdDKG88zn_DoJra8OZlyXnnXywSyd");
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE,
                "application/json");
        return headers;
    }
}
