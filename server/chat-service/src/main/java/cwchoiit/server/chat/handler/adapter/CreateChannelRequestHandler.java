package cwchoiit.server.chat.handler.adapter;

import cwchoiit.server.chat.constants.ChannelResponse;
import cwchoiit.server.chat.constants.IdKey;
import cwchoiit.server.chat.handler.request.BaseRequest;
import cwchoiit.server.chat.handler.request.CreateChannelRequest;
import cwchoiit.server.chat.handler.response.ChannelJoinNotificationResponse;
import cwchoiit.server.chat.handler.response.CreateChannelResponse;
import cwchoiit.server.chat.handler.response.ErrorResponse;
import cwchoiit.server.chat.service.ChannelService;
import cwchoiit.server.chat.service.ClientNotificationService;
import cwchoiit.server.chat.service.UserService;
import cwchoiit.server.chat.service.response.ChannelCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cwchoiit.server.chat.constants.ChannelResponse.FAILED;
import static cwchoiit.server.chat.constants.ChannelResponse.NOT_FOUND;
import static cwchoiit.server.chat.constants.MessageType.CHANNEL_CREATE_REQUEST;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChannelRequestHandler implements RequestHandler {

    private static final int THREAD_POOL_SIZE = 10;

    private final ChannelService channelService;
    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @Override
    public String messageType() {
        return CHANNEL_CREATE_REQUEST;
    }

    @Override
    public void handle(BaseRequest request, WebSocketSession session) {
        if (request instanceof CreateChannelRequest createChannelRequest) {
            Long creatorId = (Long) session.getAttributes().get(IdKey.USER_ID.getValue());

            // 채널 메시지 대상자들 찾기
            List<Long> participantIds = userService.findUserIdsByUsernames(createChannelRequest.getParticipantUsernames());

            // 채널 메시지 대상자가 없는 경우 처리
            if (participantIds.isEmpty()) {
                clientNotificationService.sendMessage(
                        session,
                        creatorId,
                        new ErrorResponse(CHANNEL_CREATE_REQUEST, NOT_FOUND.getMessage())
                );
                return;
            }

            try {
                // 다이렉트 채널 생성
                Pair<Optional<ChannelCreateResponse>, ChannelResponse> result = channelService.createGroupChannel(
                        creatorId,
                        participantIds,
                        createChannelRequest.getTitle()
                );

                result.getFirst().ifPresentOrElse(channel -> {
                            // 다이렉트 채널 생성자에게 노티
                            clientNotificationService.sendMessage(
                                    session,
                                    creatorId,
                                    new CreateChannelResponse(channel.channelId(), channel.title())
                            );

                            // 다이렉트 채널 대상자에게 노티
                            participantIds.forEach(participantId ->
                                    CompletableFuture.runAsync(() ->
                                            clientNotificationService.sendMessage(
                                                    participantId,
                                                    new ChannelJoinNotificationResponse(channel.channelId(), channel.title())
                                            ), pool)
                            );
                        },
                        // 다이렉트 채널 생성 실패 시 노티
                        () -> clientNotificationService.sendMessage(
                                session,
                                creatorId,
                                new ErrorResponse(CHANNEL_CREATE_REQUEST, result.getSecond().getMessage())
                        )
                );
            } catch (Exception e) {
                // 채널 생성 중 예외 발생
                log.error("[handle] Exception occurred while creating channel.", e);
                clientNotificationService.sendMessage(
                        session,
                        creatorId,
                        new ErrorResponse(CHANNEL_CREATE_REQUEST, FAILED.getMessage())
                );
            }
        }
    }
}
