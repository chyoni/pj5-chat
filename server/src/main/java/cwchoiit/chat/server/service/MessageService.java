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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelService channelService;
    private final UserService userService;
    private final WebSocketSessionManager sessionManager;

    @Transactional
    public void sendMessage(Long channelId, Long senderId, String content) {
        messageRepository.save(Message.create(senderId, content));

        String senderUsername = userService.findUsernameByUserId(senderId).orElseThrow();

        channelService.findParticipantIds(channelId).stream()
                .filter(participant -> !senderId.equals(participant.userId()))
                .forEach(participant -> {
                    if (channelService.isOnline(participant.userId(), channelId)) {
                        WebSocketSession participantSession = sessionManager.findSessionByUserId(participant.userId());
                        if (participantSession != null) {
                            sessionManager.sendMessage(
                                    participantSession,
                                    new MessageResponse(channelId, senderUsername, content)
                            );
                        }
                    }
                });
    }
}
