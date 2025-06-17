package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.InviteRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.InviteNotificationResponse;
import cwchoiit.server.chat.handler.response.InviteResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static cwchoiit.server.chat.constants.MessageType.INVITE_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRequestHandler implements RequestHandler {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return INVITE_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof InviteRequest inviteRequest) {
            Long inviterUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            Pair<Optional<Long>, String> result = userConnectionService.invite(inviterUserId, inviteRequest.getConnectionInviteCode());

            result.getFirst().ifPresentOrElse(partnerUserId -> {

                // 초대 신청한 사람에게 응답 메시지
                clientNotificationService.sendMessage(
                        session,
                        inviterUserId,
                        new InviteResponse(inviteRequest.getConnectionInviteCode(), UserConnectionStatus.PENDING)
                );

                // 초대 받은 사람에게 초대 요청 메시지
                clientNotificationService.sendMessage(
                        partnerUserId,
                        new InviteNotificationResponse(result.getSecond())
                );
            }, () -> {
                // 초대 요청 중 에러가 발생한 경우, 초대 신청한 사람에게 에러 메시지
                clientNotificationService.sendMessage(
                        session,
                        inviterUserId,
                        new ErrorResponse(INVITE_REQUEST, result.getSecond())
                );
            });
        }
    }
}
