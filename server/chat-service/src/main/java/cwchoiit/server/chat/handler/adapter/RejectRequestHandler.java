package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.RejectRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.RejectResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.REJECT_REQUEST;
import static cwchoiit.server.chat.constants.UserConnectionStatus.REJECTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return REJECT_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof RejectRequest rejectRequest) {
            Long declinerId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            Pair<Boolean, String> result = userConnectionService.reject(declinerId, rejectRequest.getInviterUsername());

            if (result.getFirst()) { // 초대 거절에 성공한 경우
                clientNotificationService.sendMessage(
                        session,
                        declinerId,
                        new RejectResponse(rejectRequest.getInviterUsername(), REJECTED)
                );
            } else { // 초대 거절에 실패한 경우
                String errorMessage = result.getSecond();
                clientNotificationService.sendMessage(
                        session,
                        declinerId,
                        new ErrorResponse(REJECT_REQUEST, errorMessage)
                );
            }
        }
    }
}
