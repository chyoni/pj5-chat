package cwchoiit.chat.server.service;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.Channel;
import cwchoiit.chat.server.entity.UserChannel;
import cwchoiit.chat.server.repository.ChannelRepository;
import cwchoiit.chat.server.repository.UserChannelRepository;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
import cwchoiit.chat.server.service.response.ChannelParticipantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cwchoiit.chat.server.constants.ChannelResponse.*;
import static cwchoiit.chat.server.constants.UserConnectionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {

    private final UserChannelRepository userChannelRepository;
    private final UserConnectionService userConnectionService;
    private final ChannelRepository channelRepository;
    private final StringRedisTemplate redisTemplate;

    private final int TIME_TO_LIVE = 300;

    @Transactional
    public Pair<Optional<ChannelCreateResponse>, ChannelResponse> createDirectChannel(Long creatorId,
                                                                                      Long participantId,
                                                                                      String title) {
        if (title == null || title.isBlank()) {
            log.warn("[create] Channel title is blank");
            return Pair.of(Optional.empty(), INVALID_ARGS);
        }

        if (userConnectionService.findStatus(creatorId, participantId) != ACCEPTED) {
            log.warn("[create] Create direct channel failed. Creator and participant are not connected.");
            return Pair.of(Optional.empty(), NOT_ALLOWED);
        }

        Channel channel = channelRepository.save(Channel.createDirectChannel(title));
        userChannelRepository.saveAll(
                List.of(
                        UserChannel.create(creatorId, channel.getChannelId()),
                        UserChannel.create(participantId, channel.getChannelId())
                )
        );

        return Pair.of(
                Optional.of(ChannelCreateResponse.directChannel(channel.getChannelId(), channel.getTitle())),
                SUCCESS
        );
    }

    public Pair<Optional<String>, ChannelResponse> enter(Long userId, Long channelId) {
        if (!isJoined(channelId, userId)) {
            log.warn("[enter] User is not joined to channel: {}", channelId);
            return Pair.of(Optional.empty(), NOT_JOINED);
        }
        String channelTitle = channelRepository.findById(channelId)
                .map(Channel::getTitle)
                .orElseThrow();

        setActiveChannel(userId, channelId);

        return Pair.of(Optional.of(channelTitle), SUCCESS);
    }

    public boolean isJoined(Long channelId, Long userId) {
        return userChannelRepository.existsByUserIdAndChannelId(userId, channelId);
    }

    public boolean isOnline(Long userId, Long channelId) {
        String activeChannel = redisTemplate.opsForValue().get(generateKey(userId));
        if (activeChannel == null) {
            return false;
        }
        return activeChannel.equals(channelId.toString());
    }

    public List<ChannelParticipantResponse> findParticipantIds(Long channelId) {
        return userChannelRepository.findUserIdsByChannelId(channelId).stream()
                .map(ChannelParticipantResponse::of)
                .toList();
    }

    public void setActiveChannel(Long userId, Long channelId) {
        redisTemplate.opsForValue().set(
                generateKey(userId),
                channelId.toString(),
                TIME_TO_LIVE,
                TimeUnit.SECONDS
        );
    }

    public void refreshActiveChannel(Long userId) {
        redisTemplate.expire(generateKey(userId), TIME_TO_LIVE, TimeUnit.SECONDS);
    }

    private String generateKey(Long userId) {
        return "chat:user_id:%s:channel".formatted(userId);
    }
}
