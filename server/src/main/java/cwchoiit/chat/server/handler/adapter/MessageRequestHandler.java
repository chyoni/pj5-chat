package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.MessageRequest;
import cwchoiit.chat.server.handler.response.MessageResponse;
import cwchoiit.chat.server.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.IdKey.USER_ID;
import static cwchoiit.chat.server.constants.MessageType.MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRequestHandler implements RequestHandler {

    private final MessageService messageService;

    @Override
    public String messageType() {
        return MESSAGE;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof MessageRequest messageRequest) {
            Long senderId = (Long) session.getAttributes().get(USER_ID.getValue());
            messageService.sendMessage(
                    messageRequest.getChannelId(),
                    senderId,
                    messageRequest.getContent()
            );
        }
    }
}
