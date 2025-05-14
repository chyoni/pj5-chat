package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.entity.Message;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.handler.response.MessageResponse;
import cwchoiit.chat.server.repository.MessageRepository;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.MessageType.MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRequestHandler implements RequestHandler {

    private final MessageRepository messageRepository;
    private final WebSocketSessionManager sessionManager;

    @Override
    public boolean isSupported(String type) {
        return type.equals(MESSAGE);
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof MessageRequest messageRequest) {
            messageRepository.save(Message.create(messageRequest.getUsername(), messageRequest.getContent()));
            sessionManager.getSessions().stream()
                    .filter(s -> !s.getId().equals(session.getId()))
                    .forEach(s ->
                            sessionManager.sendMessage(
                                    s,
                                    new MessageResponse(messageRequest.getUsername(), messageRequest.getContent())
                            )
                    );
        }
    }
}
