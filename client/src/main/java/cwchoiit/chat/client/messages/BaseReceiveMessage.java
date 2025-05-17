package cwchoiit.chat.client.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cwchoiit.chat.client.messages.receive.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static cwchoiit.chat.client.constants.MessageType.*;

@Getter
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
        @JsonSubTypes.Type(value = ErrorReceiveMessage.class, name = ERROR),
        @JsonSubTypes.Type(value = FetchConnectionsReceiveMessage.class, name = FETCH_CONNECTIONS_RESPONSE),
        @JsonSubTypes.Type(value = FetchUserInviteCodeReceiveMessage.class, name = FETCH_USER_INVITE_CODE_RESPONSE),
        @JsonSubTypes.Type(value = DisconnectReceiveMessage.class, name = DISCONNECT_RESPONSE),
        @JsonSubTypes.Type(value = ChatMessageReceiveMessage.class, name = MESSAGE),
        @JsonSubTypes.Type(value = RejectReceiveMessage.class, name = REJECT_RESPONSE),
})
@RequiredArgsConstructor
public abstract class BaseReceiveMessage {
    private final String type;
}
