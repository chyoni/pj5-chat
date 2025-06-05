package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.FetchUserInviteCodeRequest;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.handler.response.FetchUserInviteCodeResponse;
import cwchoiit.chat.server.service.ClientNotificationService;
import cwchoiit.chat.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.chat.server.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;

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
