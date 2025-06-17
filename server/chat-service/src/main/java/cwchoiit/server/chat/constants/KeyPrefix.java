package cwchoiit.server.chat.constants;

public interface KeyPrefix {

    String USER_SESSION = "cwchoiit:chat:user:session";
    String USERNAME = "cwchoiit:chat:user:username";
    String USER_ID = "cwchoiit:chat:user:user_id";
    String USER = "cwchoiit:chat:user";
    String USER_INVITECODE = "cwchoiit:chat:user:user_invitecode";

    String CONNECTION_STATUS = "cwchoiit:chat:connection:status";
    String CONNECTIONS_STATUS = "cwchoiit:chat:connections:status";
    String INVITER_USER_ID = "cwchoiit:chat:connection:inviter_id";

    String CHANNEL = "cwchoiit:chat:channel";
    String CHANNELS = "cwchoiit:chat:channels";
    String CHANNEL_INVITECODE = "cwchoiit:chat:channel_invitecode";
    String JOINED_CHANNEL = "cwchoiit:chat:joined_channel";
    String PARTICIPANT_IDS = "cwchoiit:chat:participant_ids";
}
