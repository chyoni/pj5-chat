package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.Constants;
import cwchoiit.chat.server.handler.request.AcceptRequest;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.response.AcceptNotificationResponse;
import cwchoiit.chat.server.handler.response.AcceptResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.UserConnectionService;
import cwchoiit.chat.server.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.chat.server.constants.MessageType.ACCEPT_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final WebSocketSessionManager sessionManager;

    @Override
    public String messageType() {
        return ACCEPT_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof AcceptRequest acceptRequest) {
            Long acceptorId = (Long) session.getAttributes().get(Constants.USER_ID.getValue());
            Pair<Optional<Long>, String> result = userConnectionService.accept(acceptorId, acceptRequest.getInviterUsername());

            result.getFirst().ifPresentOrElse(
                    inviterUserId -> {
                        // 초대를 받고 수락을 한 사용자에게 안내 메시지 (To. Acceptor)
                        sessionManager.sendMessage(
                                session,
                                new AcceptResponse(acceptRequest.getInviterUsername())
                        );

                        // 초대한 사람에게 결과를 보내주는 메시지 (To. Inviter)
                        String acceptorUsername = result.getSecond();
                        sessionManager.sendMessage(
                                sessionManager.findSessionByUserId(inviterUserId),
                                new AcceptNotificationResponse(acceptorUsername)
                        );
                    },
                    () -> {
                        // 초대에 대한 수락을 하려 했으나, 에러가 발생한 경우 (To. Acceptor)
                        String errorMessage = result.getSecond();
                        sessionManager.sendMessage(session, new ErrorResponse(ACCEPT_REQUEST, errorMessage));
                    }
            );
        }
    }
}
