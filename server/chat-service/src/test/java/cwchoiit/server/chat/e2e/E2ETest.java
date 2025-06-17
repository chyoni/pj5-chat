package cwchoiit.server.chat.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.auth.core.RestApiLoginAuthFilter;
import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.constants.ChannelResponse;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.entity.UserConnection;
import cwchoiit.server.chat.repository.UserConnectionRepository;
import cwchoiit.server.chat.service.*;
import cwchoiit.server.chat.service.request.UserRegisterRequest;
import cwchoiit.server.chat.service.response.ChannelCreateResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static cwchoiit.server.chat.constants.KeyPrefix.CONNECTION_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class E2ETest extends SpringBootTestConfiguration {

    @LocalServerPort
    int port;

    int restPort = 8080;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserService userService;
    @Autowired
    ChannelService channelService;
    @Autowired
    MessageService messageService;
    @Autowired
    UserConnectionService userConnectionService;
    @Autowired
    UserConnectionRepository userConnectionRepository;
    @Autowired
    TransactionTemplate transactionTemplate;
    @Autowired
    CacheService cacheService;

    Client createClient(String sessionId) throws ExecutionException, InterruptedException, URISyntaxException {
        String url = String.format("ws://localhost:%d/ws/v1/message", port);

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        webSocketHttpHeaders.add("Cookie", "SESSION=%s".formatted(sessionId));

        StandardWebSocketClient client = new StandardWebSocketClient();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        WebSocketSession newSession = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
                queue.put(message.getPayload());
            }
        }, webSocketHttpHeaders, new URI(url)).get();

        return new Client(queue, newSession);
    }

    void register(String username, String password) throws JsonProcessingException {
        String registerURL = String.format("http://localhost:%s/api/v1/user/register", restPort);

        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add("Content-Type", "application/json");

        HttpHeaders httpHeaders = new HttpHeaders(requestHeaders);

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(userRegisterRequest);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, httpHeaders);

        new RestTemplate().postForEntity(registerURL, httpEntity, String.class);
    }

    void unregister(String sessionId) {
        String registerURL = String.format("http://localhost:%s/api/v1/user/unregister", restPort);

        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Cookie", String.format("SESSION=%s", sessionId));

        HttpHeaders httpHeaders = new HttpHeaders(requestHeaders);

        HttpEntity<UserRegisterRequest> httpEntity = new HttpEntity<>(httpHeaders);

        new RestTemplate().postForEntity(registerURL, httpEntity, String.class);
    }

    String login(String username, String password) throws JsonProcessingException {
        String loginURL = String.format("http://localhost:%s/api/v1/auth/login", restPort);
        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();
        requestHeaders.add("Content-Type", "application/json");
        RestApiLoginAuthFilter.LoginRequest loginRequest = new RestApiLoginAuthFilter.LoginRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(loginRequest);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, new HttpHeaders(requestHeaders));

        return new RestTemplate().postForObject(loginURL, httpEntity, String.class);
    }

    @Test
    @DisplayName("일대일 채팅 기본 연결 및 메시지 전송 테스트")
    void directChat() throws ExecutionException, InterruptedException, IOException, URISyntaxException {
        // 회원 가입
        register("testUserA", "testUserAPassword");
        register("testUserB", "testUserBPassword");

        // 유저 조회
        Long userAId = userService.findUserIdByUsername("testUserA").orElseThrow();
        Long userBId = userService.findUserIdByUsername("testUserB").orElseThrow();

        // 로그인
        String sessionIdA = login("testUserA", "testUserAPassword");
        String sessionIdB = login("testUserB", "testUserBPassword");

        // 로그인 한 유저로 클라이언트 만들기
        Client userA = createClient(sessionIdA);
        Client userB = createClient(sessionIdB);

        // 유저 간 연결 상태 만들기
        String userBInviteCode = userService.findInviteCodeByUserId(userBId).orElseThrow();
        userConnectionService.invite(userAId, userBInviteCode);

        // 서비스를 통해 유저 간 연결상태를 만들면, 내부 로직에서 레코드 락으로 락을 점유하고 있기 때문에 이 전체 테스트에서 여전히 그 락이 유효해짐. 그래서 하단에 회원 탈퇴가 불가능함.
        // 따라서, 데이터베이스에 직접 연결 상태를 저장하고 flush
        transactionTemplate.execute(status -> {
            UserConnection userConnection = userConnectionRepository.findUserConnectionBy(userAId, userBId).orElseThrow();
            userConnection.changeStatus(UserConnectionStatus.ACCEPTED);
            return null;
        });

        // 캐시 무효화 - (위에서 상태를 변경했지만 테스트 코드로 변경했기 때문에, 실제 서비스 코드에서 캐시가 그대로 남아있을 수 있다)
        long lowerUserId = Long.min(userAId, userBId);
        long higherUserId = Long.max(userAId, userBId);
        String cacheKey = cacheService.generateKey(
                CONNECTION_STATUS,  // CONNECTION_STATUS 상수값
                String.valueOf(lowerUserId),
                String.valueOf(higherUserId)
        );
        cacheService.delete(cacheKey);

        // 유저끼리 채팅 만들기
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> testChannel =
                channelService.createDirectChannel(userAId, userBId, "testChannel");

        // 채널 관련 검증
        assertThat(testChannel.getFirst()).isNotEmpty();
        assertThat(testChannel.getSecond()).isEqualTo(ChannelResponse.SUCCESS);
        assertThat(testChannel.getFirst().get().headCount()).isEqualTo(2);

        // 채널에 유저들 진입
        channelService.enter(userAId, testChannel.getFirst().get().channelId());
        channelService.enter(userBId, testChannel.getFirst().get().channelId());

        // 메시지 전송
        messageService.sendMessage(testChannel.getFirst().get().channelId(), userAId, "Im A");
        messageService.sendMessage(testChannel.getFirst().get().channelId(), userBId, "Im B");

        // 메시지 획득
        String fromUserAMessage = userB.getQueue().poll(1, TimeUnit.SECONDS);
        String fromUserBMessage = userA.getQueue().poll(1, TimeUnit.SECONDS);

        // 메시지 역직렬화
        MessageResponse messageResFromA = Serializer.deserialize(fromUserAMessage, MessageResponse.class).orElseThrow();
        MessageResponse messageResFromB = Serializer.deserialize(fromUserBMessage, MessageResponse.class).orElseThrow();

        // 메시지 획득 내용 검증
        assertThat(messageResFromA.getContent()).isEqualTo("Im A");
        assertThat(messageResFromA.getUsername()).isEqualTo("testUserA");
        assertThat(messageResFromA.getChannelId()).isEqualTo(1);
        assertThat(messageResFromB.getContent()).isEqualTo("Im B");
        assertThat(messageResFromB.getUsername()).isEqualTo("testUserB");
        assertThat(messageResFromB.getChannelId()).isEqualTo(1);

        // 회원 탈퇴
        unregister(sessionIdA);
        unregister(sessionIdB);

        // 회원 탈퇴 후 세션 닫기
        userA.getSession().close();
        userB.getSession().close();
    }

    @Getter
    @ToString
    @AllArgsConstructor
    static class Client {
        private BlockingQueue<String> queue;
        private WebSocketSession session;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    static class MessageResponse {
        private final String type = "MESSAGE";
        private Long channelId;
        private String username;
        private String content;
    }
}