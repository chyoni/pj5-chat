package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.FetchUserInviteCodeRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.FetchUserInviteCodeResponse;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeRequestHandler implements RequestHandler {

    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return FETCH_USER_INVITE_CODE_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof FetchUserInviteCodeRequest) {
            Long requestUserId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());
            userService.findInviteCodeByUserId(requestUserId)
                    .ifPresentOrElse(
                            inviteCode -> clientNotificationService.sendMessage(
                                    session,
                                    requestUserId,
                                    new FetchUserInviteCodeResponse(inviteCode)
                            ),
                            () -> clientNotificationService.sendMessage(
                                    session,
                                    requestUserId,
                                    new ErrorResponse(FETCH_USER_INVITE_CODE_REQUEST, "No invite code found.")
                            )
                    );
        }
    }
}
