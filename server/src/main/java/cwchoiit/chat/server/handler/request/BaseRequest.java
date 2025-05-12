package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cwchoiit.chat.server.constants.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageRequest.class, name = MessageType.MESSAGE),
        @JsonSubTypes.Type(value = KeepAliveRequest.class, name = MessageType.KEEP_ALIVE)
})
@RequiredArgsConstructor
public abstract class BaseRequest {
    private final String type;
}
