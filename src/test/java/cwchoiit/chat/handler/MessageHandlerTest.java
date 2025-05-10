package cwchoiit.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cwchoiit.chat.dto.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MessageHandlerTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Getter
    @ToString
    @AllArgsConstructor
    static class Client {
        private BlockingQueue<String> queue;
        private WebSocketSession session;
    }

    static Client createClient(String url) throws ExecutionException, InterruptedException {
        StandardWebSocketClient client = new StandardWebSocketClient();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        WebSocketSession newSession = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
                queue.put(message.getPayload());
            }
        }, url).get();

        return new Client(queue, newSession);
    }

    @Test
    @DisplayName("일대일 채팅 기본 연결 및 메시지 전송 테스트")
    void directChat() throws ExecutionException, InterruptedException, IOException {
        String url = String.format("ws://localhost:%d/ws/v1/message", port);

        Client leftSession = createClient(url);
        Client rightSession = createClient(url);

        leftSession.getSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("leftSession", "안녕하세요"))));
        rightSession.getSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("rightSession", "Hello"))));

        String fromLeftMessage = rightSession.getQueue().poll(1, TimeUnit.SECONDS);
        String fromRightMessage = leftSession.getQueue().poll(1, TimeUnit.SECONDS);

        assertThat(fromLeftMessage).contains("안녕하세요");
        assertThat(fromRightMessage).contains("Hello");

        leftSession.getSession().close();
        rightSession.getSession().close();
    }

    @Test
    @DisplayName("그룹 채팅 기본 테스트")
    void groupChat() throws ExecutionException, InterruptedException, IOException {
        String url = String.format("ws://localhost:%d/ws/v1/message", port);

        Client sessionOne = createClient(url);
        Client sessionTwo = createClient(url);
        Client sessionThree = createClient(url);

        sessionOne.getSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("sessionOne", "Hello1"))));
        sessionTwo.getSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("sessionTwo", "Hello2"))));
        sessionThree.getSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("sessionThree", "Hello3"))));

        String sessionOneReceivedMessage = sessionOne.getQueue().poll(1, TimeUnit.SECONDS) + sessionOne.getQueue().poll(1, TimeUnit.SECONDS);
        String sessionTwoReceivedMessage = sessionTwo.getQueue().poll(1, TimeUnit.SECONDS) + sessionTwo.getQueue().poll(1, TimeUnit.SECONDS);
        String sessionThreeReceivedMessage = sessionThree.getQueue().poll(1, TimeUnit.SECONDS) + sessionThree.getQueue().poll(1, TimeUnit.SECONDS);

        assertThat(sessionOneReceivedMessage).contains("sessionTwo");
        assertThat(sessionOneReceivedMessage).contains("sessionThree");

        assertThat(sessionTwoReceivedMessage).contains("sessionOne");
        assertThat(sessionTwoReceivedMessage).contains("sessionThree");

        assertThat(sessionThreeReceivedMessage).contains("sessionOne");
        assertThat(sessionThreeReceivedMessage).contains("sessionTwo");


        assertThat(sessionOne.getQueue().isEmpty()).isTrue();
        assertThat(sessionTwo.getQueue().isEmpty()).isTrue();
        assertThat(sessionThree.getQueue().isEmpty()).isTrue();

        sessionOne.getSession().close();
        sessionTwo.getSession().close();
        sessionThree.getSession().close();
    }
}