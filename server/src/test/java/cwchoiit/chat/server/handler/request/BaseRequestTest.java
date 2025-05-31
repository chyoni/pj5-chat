package cwchoiit.chat.server.handler.request;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cwchoiit.chat.server.constants.MessageType.*;
import static org.assertj.core.api.Assertions.assertThat;


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
                "{\"type\":\"MESSAGE\", \"content\": \"Hello!\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(MESSAGE);
        assertThat(baseRequest).isInstanceOf(MessageRequest.class);

        MessageRequest messageRequest = (MessageRequest) baseRequest;
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

    @Test
    @DisplayName("BaseRequest 클래스가 FETCH_USER_INVITE_CODE_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize5() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"FETCH_USER_INVITE_CODE_REQUEST\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(FETCH_USER_INVITE_CODE_REQUEST);
        assertThat(baseRequest).isInstanceOf(FetchUserInviteCodeRequest.class);
    }

    @Test
    @DisplayName("BaseRequest 클래스가 FETCH_CONNECTIONS_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize6() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"FETCH_CONNECTIONS_REQUEST\", \"status\": \"PENDING\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(FETCH_CONNECTIONS_REQUEST);
        assertThat(baseRequest).isInstanceOf(FetchConnectionsRequest.class);

        FetchConnectionsRequest fetchConnectionsRequest = (FetchConnectionsRequest) baseRequest;
        assertThat(fetchConnectionsRequest.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
    }

    @Test
    @DisplayName("BaseRequest 클래스가 REJECT_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize7() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"REJECT_REQUEST\", \"inviterUsername\": \"inviter\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(REJECT_REQUEST);
        assertThat(baseRequest).isInstanceOf(RejectRequest.class);

        RejectRequest rejectRequest = (RejectRequest) baseRequest;
        assertThat(rejectRequest.getInviterUsername()).isEqualTo("inviter");
    }

    @Test
    @DisplayName("BaseRequest 클래스가 DISCONNECT_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize8() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"DISCONNECT_REQUEST\", \"peerUsername\": \"peer\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(DISCONNECT_REQUEST);
        assertThat(baseRequest).isInstanceOf(DisconnectRequest.class);

        DisconnectRequest disconnectRequest = (DisconnectRequest) baseRequest;
        assertThat(disconnectRequest.getPeerUsername()).isEqualTo("peer");
    }

    @Test
    @DisplayName("BaseRequest 클래스가 CHANNEL_CREATE_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize9() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"CHANNEL_CREATE_REQUEST\", \"title\": \"title\", \"participantUsernames\": [\"p1\", \"p2\"]}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(CHANNEL_CREATE_REQUEST);
        assertThat(baseRequest).isInstanceOf(CreateChannelRequest.class);

        CreateChannelRequest createChannelRequest = (CreateChannelRequest) baseRequest;
        assertThat(createChannelRequest.getTitle()).isEqualTo("title");
        assertThat(createChannelRequest.getParticipantUsernames())
                .containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    @DisplayName("BaseRequest 클래스가 ENTER_CHANNEL_REQUEST 타입으로 메시지가 들어오는 경우 역직렬화 정상 처리")
    void deserialize10() {
        BaseRequest baseRequest = Serializer.deserialize(
                "{\"type\":\"ENTER_CHANNEL_REQUEST\", \"channelId\": \"1\"}",
                BaseRequest.class
        ).orElseThrow();

        assertThat(baseRequest.getType()).isEqualTo(ENTER_CHANNEL_REQUEST);
        assertThat(baseRequest).isInstanceOf(EnterChannelRequest.class);

        EnterChannelRequest enterChannelRequest = (EnterChannelRequest) baseRequest;
        assertThat(enterChannelRequest.getChannelId()).isEqualTo(1);
    }
}