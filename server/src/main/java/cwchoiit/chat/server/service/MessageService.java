package cwchoiit.chat.server.service;

import cwchoiit.chat.server.entity.Message;
import cwchoiit.chat.server.handler.response.MessageResponse;
import cwchoiit.chat.server.repository.MessageRepository;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private static final int THREAD_POOL_SIZE = 10;

    private final MessageRepository messageRepository;
    private final ChannelService channelService;
    private final UserService userService;
    private final WebSocketSessionManager sessionManager;

    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @Transactional
    public void sendMessage(Long channelId, Long senderId, String content) {
        messageRepository.save(Message.create(senderId, content));

        String senderUsername = userService.findUsernameByUserId(senderId).orElseThrow();

        channelService.findOnlineParticipantIds(channelId).stream()
                .filter(participant -> !senderId.equals(participant))
                .forEach(participant -> CompletableFuture.runAsync(() -> {
                            WebSocketSession participantSession = sessionManager.findSessionByUserId(participant);
                            if (participantSession != null) {
                                sessionManager.sendMessage(
                                        participantSession,
                                        new MessageResponse(channelId, senderUsername, content)
                                );
                            }
                        }, pool)
                );
    }
}
