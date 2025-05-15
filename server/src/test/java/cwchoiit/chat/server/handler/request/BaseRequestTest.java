package cwchoiit.chat.server.handler.request;

import cwchoiit.chat.serializer.Serializer;
import cwchoiit.chat.server.constants.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cwchoiit.chat.server.constants.MessageType.*;
import static org.assertj.core.api.Assertions.*;


@DisplayName("BaseRequest의 역직렬화 테스트")
class BaseRequestTest {

    @Test
    @DisplayName("BaseRequest 클래스가 INVITE_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"INVITE_REQUEST\", \"connectionInviteCode\": \"con123\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(INVITE_REQUEST);
        assertThat(baseRequest).isInstanceOf(InviteRequest.class);
    }

    @Test
    @DisplayName("BaseRequest 클래스가 KEEP_ALIVE 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize2() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"KEEP_ALIVE\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(KEEP_ALIVE);
        assertThat(baseRequest).isInstanceOf(KeepAliveRequest.class);
    }

    @Test
    @DisplayName("BaseRequest 클래스가 MESSAGE 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize3() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"MESSAGE\", \"username\": \"user1\", \"content\": \"Hello!\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(MESSAGE);
        assertThat(baseRequest).isInstanceOf(MessageRequest.class);

        MessageRequest messageRequest = (MessageRequest) baseRequest;
        assertThat(messageRequest.getUsername()).isEqualTo("user1");
        assertThat(messageRequest.getContent()).isEqualTo("Hello!");
    }

    @Test
    @DisplayName("BaseRequest 클래스가 ACCEPT_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize4() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"ACCEPT_REQUEST\", \"inviterUsername\": \"user123\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(ACCEPT_REQUEST);
        assertThat(baseRequest).isInstanceOf(AcceptRequest.class);

        AcceptRequest acceptRequest = (AcceptRequest) baseRequest;
        assertThat(acceptRequest.getInviterUsername()).isEqualTo("user123");
    }
}