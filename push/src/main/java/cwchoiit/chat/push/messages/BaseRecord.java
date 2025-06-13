package cwchoiit.chat.push.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static cwchoiit.chat.push.constants.MessageType.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteReceiveMessage.class, name = INVITE_RESPONSE),
        @JsonSubTypes.Type(value = InviteNotificationReceiveMessage.class, name = ASK_INVITE),
        @JsonSubTypes.Type(value = AcceptNotificationReceiveMessage.class, name = NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = AcceptReceiveMessage.class, name = ACCEPT_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectReceiveMessage.class, name = DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = ChatMessageReceiveMessage.class, name = MESSAGE),
        @JsonSubTypes.Type(value = RejectReceiveMessage.class, name = REJECT_RESPONSE),
        @JsonSubTypes.Type(value = CreateChannelReceiveMessage.class, name = CHANNEL_CREATE_RESPONSE),
        @JsonSubTypes.Type(value = ChannelJoinNotificationReceiveMessage.class, name = NOTIFY_CHANNEL_JOIN),
        @JsonSubTypes.Type(value = LeaveChannelReceiveMessage.class, name = LEAVE_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = QuitChannelReceiveMessage.class, name = QUIT_CHANNEL_RESPONSE),
})
public interface BaseRecord {
    String type();
}
