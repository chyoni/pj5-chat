package cwchoiit.chat.server.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.chat.server.handler.response.BaseResponse;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientNotificationService {

    private final WebSocketSessionManager sessionManager;
    private final PushService pushService;

    @PostConstruct
    public void init() {
        pushService.registerPushMessageType(INVITE_RESPONSE);
        pushService.registerPushMessageType(ASK_INVITE);
        pushService.registerPushMessageType(ACCEPT_RESPONSE);
        pushService.registerPushMessageType(NOTIFY_ACCEPT);
        pushService.registerPushMessageType(JOIN_CHANNEL_RESPONSE);
        pushService.registerPushMessageType(NOTIFY_CHANNEL_JOIN);
        pushService.registerPushMessageType(DISCONNECT_RESPONSE);
        pushService.registerPushMessageType(REJECT_RESPONSE);
        pushService.registerPushMessageType(CHANNEL_CREATE_RESPONSE);
        pushService.registerPushMessageType(QUIT_CHANNEL_RESPONSE);
    }

    public void sendMessage(WebSocketSession session, Long userId, BaseResponse response) {
        sendPayload(session, userId, response);
    }

    public void sendMessage(Long userId, BaseResponse response) {
        sendPayload(sessionManager.findSessionByUserId(userId), userId, response);
    }

    private void sendPayload(WebSocketSession session, Long userId, BaseResponse response) {
        Optional<String> serialized = Serializer.serialize(response);
        if (serialized.isEmpty()) {
            log.error("[sendPayload] Send message failed. Message Type = {}", response.getType());
            return;
        }

        String payload = serialized.get();
        if (session != null) {
            try {
                sessionManager.sendMessage(session, payload);
            } catch (IOException e) {
                pushService.pushMessage(userId, response.getType(), payload);
            }
        } else {
            pushService.pushMessage(userId, response.getType(), payload);
        }
    }
}
