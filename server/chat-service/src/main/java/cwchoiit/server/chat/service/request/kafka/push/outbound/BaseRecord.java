package cwchoiit.server.chat.service.request.kafka.push.outbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static cwchoiit.server.chat.constants.MessageType.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InviteOutboundMessage.class, name = INVITE_RESPONSE),
        @JsonSubTypes.Type(value = InviteNotificationOutboundMessage.class, name = ASK_INVITE),
        @JsonSubTypes.Type(value = AcceptNotificationOutboundMessage.class, name = NOTIFY_ACCEPT),
        @JsonSubTypes.Type(value = AcceptOutboundMessage.class, name = ACCEPT_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectOutboundMessage.class, name = DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = ChatMessageOutboundMessage.class, name = MESSAGE),
        @JsonSubTypes.Type(value = RejectOutboundMessage.class, name = REJECT_RESPONSE),
        @JsonSubTypes.Type(value = CreateChannelOutboundMessage.class, name = CHANNEL_CREATE_RESPONSE),
        @JsonSubTypes.Type(value = ChannelJoinNotificationOutboundMessage.class, name = NOTIFY_CHANNEL_JOIN),
        @JsonSubTypes.Type(value = LeaveChannelOutboundMessage.class, name = LEAVE_CHANNEL_RESPONSE),
        @JsonSubTypes.Type(value = QuitChannelOutboundMessage.class, name = QUIT_CHANNEL_RESPONSE),
})
public interface BaseRecord {
    String type();
}
