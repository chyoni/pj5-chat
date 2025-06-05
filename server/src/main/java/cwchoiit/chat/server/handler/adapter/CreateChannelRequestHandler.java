package cwchoiit.chat.server.handler.adapter;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.IdKey;
import cwchoiit.chat.server.handler.request.BaseRequest;
import cwchoiit.chat.server.handler.request.CreateChannelRequest;
import cwchoiit.chat.server.handler.response.ChannelJoinNotificationResponse;
import cwchoiit.chat.server.handler.response.CreateChannelResponse;
import cwchoiit.chat.server.handler.response.ErrorResponse;
import cwchoiit.chat.server.service.ChannelService;
import cwchoiit.chat.server.service.ClientNotificationService;
import cwchoiit.chat.server.service.UserService;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
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

import static cwchoiit.chat.server.constants.ChannelResponse.FAILED;
import static cwchoiit.chat.server.constants.ChannelResponse.NOT_FOUND;
import static cwchoiit.chat.server.constants.MessageType.CHANNEL_CREATE_REQUEST;

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
