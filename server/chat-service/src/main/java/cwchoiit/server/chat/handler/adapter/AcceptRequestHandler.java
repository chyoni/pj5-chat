package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.AcceptRequest;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.response.AcceptNotificationResponse;
import cwchoiit.server.chat.handler.response.AcceptResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.ACCEPT_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return ACCEPT_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof AcceptRequest acceptRequest) {
            Long acceptorId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            Pair<Optional<Long>, String> result = userConnectionService.accept(acceptorId, acceptRequest.getInviterUsername());

            result.getFirst().ifPresentOrElse(
                    inviterUserId -> {
                        // 초대를 받고 수락을 한 사용자에게 안내 메시지 (To. Acceptor)
                        clientNotificationService.sendMessage(
                                session,
                                acceptorId,
                                new AcceptResponse(acceptRequest.getInviterUsername())
                        );

                        // 초대한 사람에게 결과를 보내주는 메시지 (To. Inviter)
                        String acceptorUsername = result.getSecond();
                        clientNotificationService.sendMessage(
                                inviterUserId,
                                new AcceptNotificationResponse(acceptorUsername)
                        );
                    },
                    () -> {
                        // 초대에 대한 수락을 하려 했으나, 에러가 발생한 경우 (To. Acceptor)
                        String errorMessage = result.getSecond();
                        clientNotificationService.sendMessage(
                                session,
                                acceptorId,
                                new ErrorResponse(ACCEPT_REQUEST, errorMessage)
                        );
                    }
            );
        }
    }
}
