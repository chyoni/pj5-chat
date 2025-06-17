package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.constants.MessageType;
import cwchoiit.server.chat.entity.Message;
import cwchoiit.server.chat.handler.response.MessageResponse;
import cwchoiit.server.chat.service.request.kafka.push.outbound.ChatMessageOutboundMessage;
import cwchoiit.server.chat.service.response.ChannelParticipantResponse;
import cwchoiit.server.chat.session.WebSocketSessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int THREAD_POOL_SIZE = 10;

    private final MessageCommandService messageCommandService;
    private final ChannelService channelService;
    private final UserService userService;
    private final PushService pushService;
    private final WebSocketSessionManager sessionManager;

    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @PostConstruct
    public void init() {
        pushService.registerPushMessageType(MessageType.MESSAGE, ChatMessageOutboundMessage.class);
    }

    public void sendMessage(Long channelId, Long senderId, String content) {
        MessageResponse messageResponse = new MessageResponse(
                channelId,
                userService.findUsernameByUserId(senderId).orElseThrow(),
                content
        );

        Optional<String> serialized = Serializer.serialize(messageResponse);
        if (serialized.isEmpty()) {
            log.error("[sendMessage] Send message failed. Message Type = {}", messageResponse.getType());
            return;
        }

        String payload = serialized.get();

        messageCommandService.saveMessage(Message.create(senderId, content));

        List<Long> participantIds = channelService.findParticipantIds(channelId).stream()
                .map(ChannelParticipantResponse::userId)
                .toList();
        List<Long> onlineParticipantIds = channelService.findOnlineParticipantIds(channelId, participantIds);

        for (int i = 0; i < participantIds.size(); i++) {
            Long participantId = participantIds.get(i);
            if (senderId.equals(participantId)) {
                continue;
            }
            if (onlineParticipantIds.get(i) != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        WebSocketSession session = sessionManager.findSessionByUserId(participantId);
                        if (session != null) {
                            sessionManager.sendMessage(session, payload);
                        } else {
                            pushService.pushMessage(participantId, MessageType.MESSAGE, payload);
                        }
                    } catch (Exception e) {
                        pushService.pushMessage(participantId, MessageType.MESSAGE, payload);
                    }
                }, pool);
            } else {
                pushService.pushMessage(participantId, MessageType.MESSAGE, payload);
            }
        }
    }
}
