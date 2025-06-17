package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.FetchChannelInviteCodeRequest;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.handler.response.FetchChannelInviteCodeResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static cwchoiit.server.chat.constants.IdKey.USER_ID;
import static cwchoiit.server.chat.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestHandler implements RequestHandler {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public String messageType() {
        return FETCH_CHANNEL_INVITE_CODE_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof FetchChannelInviteCodeRequest fetchChannelInviteCodeRequest) {
            Long callerId = (Long) session.getAttributes().get(USER_ID.getValue());

            if (channelService.isJoined(fetchChannelInviteCodeRequest.getChannelId(), callerId)) {
                channelService.findInviteCode(fetchChannelInviteCodeRequest.getChannelId())
                        .ifPresentOrElse(
                                inviteCode -> clientNotificationService.sendMessage(
                                        session,
                                        callerId,
                                        new FetchChannelInviteCodeResponse(fetchChannelInviteCodeRequest.getChannelId(), inviteCode)
                                ),
                                () -> clientNotificationService.sendMessage(
                                        session,
                                        callerId,
                                        new ErrorResponse(FETCH_CHANNEL_INVITE_CODE_REQUEST, "No invite code found.")
                                )
                        );
            } else {
                clientNotificationService.sendMessage(
                        session,
                        callerId,
                        new ErrorResponse(FETCH_CHANNEL_INVITE_CODE_REQUEST, "You are not joined to this channel.")
                );
            }
        }
    }
}
