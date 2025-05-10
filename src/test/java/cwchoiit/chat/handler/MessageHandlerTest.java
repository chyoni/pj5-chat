package cwchoiit.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cwchoiit.chat.dto.Message;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MessageHandlerTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("일대일 채팅 기본 연결 및 메시지 전송 테스트")
    void directChat() throws ExecutionException, InterruptedException, IOException {
        String url = String.format("ws://localhost:%d/ws/v1/message", port);

        StandardWebSocketClient leftClient = new StandardWebSocketClient();
        BlockingQueue<String> leftQueue = new ArrayBlockingQueue<>(1);


        WebSocketSession leftWebSocketSession = leftClient.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                leftQueue.put(message.getPayload());
            }
        }, url).get();

        StandardWebSocketClient rightClient = new StandardWebSocketClient();
        BlockingQueue<String> rightQueue = new ArrayBlockingQueue<>(1);
        WebSocketSession rightWebSocketSession = rightClient.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                rightQueue.put(message.getPayload());
            }
        }, url).get();

        leftWebSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("안녕하세요"))));
        rightWebSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(new Message("Hello"))));

        String fromLeftMessage = rightQueue.poll(1, TimeUnit.SECONDS);
        String fromRightMessage = leftQueue.poll(1, TimeUnit.SECONDS);

        assertThat(fromLeftMessage).contains("안녕하세요");
        assertThat(fromRightMessage).contains("Hello");

        leftWebSocketSession.close();
        rightWebSocketSession.close();
    }
}