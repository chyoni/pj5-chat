package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cwchoiit.chat.server.constants.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이 클래스는, 사용자로부터 메시지가 들어올 때 타입이 BaseRequest 형태의 타입으로 들어온다.
 * 그런데, 이 클래스를 상속받는 두 가지 타입이 있다. {@link MessageRequest}, {@link KeepAliveRequest}.
 * <p>
 * 이 두가지 타입 중 어떤 것으로 JSON을 역직렬화할지 이해하려면 아래 작성한 {@code @JsonTypeInfo}, {@code @JsonSubTypes} 가 필요하다.
 */
@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // "type" 필드 값을 문자열 이름으로 인식하겠다는 뜻 ("MESSAGE", "KEEP_ALIVE")
        include = JsonTypeInfo.As.PROPERTY, // JSON 안에 "type" 이라는 속성을 포함시킨다는 뜻
        property = "type" // 필드 이름이 "type" 이라는 뜻
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageRequest.class, name = MessageType.MESSAGE), // 위에서 명시한 "type"의 값이 "MESSAGE"라면 MessageRequest로 역직렬화
        @JsonSubTypes.Type(value = KeepAliveRequest.class, name = MessageType.KEEP_ALIVE), // 위에서 명시한 "type"의 값이 "KEEP_ALIVE"라면 KeepAliveRequest로 역직렬화
        @JsonSubTypes.Type(value = InviteRequest.class, name = MessageType.INVITE_REQUEST), // 위에서 명시한 "type"의 값이 "INVITE_REQUEST"라면 InviteRequest로 역직렬화
        @JsonSubTypes.Type(value = AcceptRequest.class, name = MessageType.ACCEPT_REQUEST), // 위에서 명시한 "type"의 값이 "ACCEPT_REQUEST"라면 AcceptRequest로 역직렬화
        @JsonSubTypes.Type(value = RejectRequest.class, name = MessageType.REJECT_REQUEST), // 위에서 명시한 "type"의 값이 "REJECT_REQUEST"라면 RejectRequest로 역직렬화
        @JsonSubTypes.Type(value = FetchUserInviteCodeRequest.class, name = MessageType.FETCH_USER_INVITE_CODE_REQUEST), // 위에서 명시한 "type"의 값이 "FETCH_USER_INVITE_CODE_REQUEST"라면 FetchUserInviteCodeRequest로 역직렬화
        @JsonSubTypes.Type(value = FetchConnectionsRequest.class, name = MessageType.FETCH_CONNECTIONS_REQUEST), // 위에서 명시한 "type"의 값이 "FETCH_CONNECTIONS_REQUEST"라면 FetchConnectionsRequest 역직렬화
        @JsonSubTypes.Type(value = DisconnectRequest.class, name = MessageType.DISCONNECT_REQUEST) // 위에서 명시한 "type"의 값이 "DISCONNECT_REQUEST"라면 DisconnectRequest 역직렬화
})
@RequiredArgsConstructor
public abstract class BaseRequest {
    private final String type;
}
