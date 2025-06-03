package cwchoiit.chat.server.service;

import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.entity.Channel;
import cwchoiit.chat.server.entity.UserChannel;
import cwchoiit.chat.server.repository.ChannelRepository;
import cwchoiit.chat.server.repository.UserChannelRepository;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
import cwchoiit.chat.server.service.response.ChannelParticipantResponse;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static cwchoiit.chat.server.constants.ChannelResponse.*;
import static cwchoiit.chat.server.constants.UserConnectionStatus.ACCEPTED;

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
    private final int MAX_CHANNEL_HEAD_COUNT = 100;

    @Transactional
    public Pair<Optional<ChannelCreateResponse>, ChannelResponse> createDirectChannel(Long creatorId,
                                                                                      Long participantId,
                                                                                      String title) {
        if (title == null || title.isBlank()) {
            log.warn("[createDirectChannel] Channel title is blank");
            return Pair.of(Optional.empty(), INVALID_ARGS);
        }

        if (userConnectionService.findStatus(creatorId, participantId) != ACCEPTED) {
            log.warn("[createDirectChannel] Create direct channel failed. Creator and participant are not connected.");
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

    @Transactional
    public Pair<Optional<ChannelCreateResponse>, ChannelResponse> createGroupChannel(Long creatorId,
                                                                                     List<Long> participantIds,
                                                                                     String title) {
        if (title == null || title.isBlank()) {
            log.warn("[createGroupChannel] Channel title is blank");
            return Pair.of(Optional.empty(), INVALID_ARGS);
        }

        int headCount = participantIds.size() + 1;
        if (headCount > MAX_CHANNEL_HEAD_COUNT) {
            log.warn("[createGroupChannel] Channel head count is over limit: {}", MAX_CHANNEL_HEAD_COUNT);
            return Pair.of(Optional.empty(), OVER_LIMIT);
        }

        if (userConnectionService.countConnectionByStatus(creatorId, participantIds, ACCEPTED) != participantIds.size()) {
            log.warn("[createGroupChannel] Create group channel failed. Creator and participants are not connected.");
            return Pair.of(Optional.empty(), NOT_ALLOWED);
        }

        Channel channel = channelRepository.save(Channel.create(title, headCount));

        List<UserChannel> channels = Stream.concat(participantIds.stream(), Stream.of(creatorId))
                .map(userId -> UserChannel.create(userId, channel.getChannelId()))
                .toList();

        userChannelRepository.saveAll(channels);

        return Pair.of(
                Optional.of(
                        ChannelCreateResponse.groupChannel(
                                channel.getChannelId(),
                                channel.getTitle(),
                                headCount
                        )
                ),
                SUCCESS
        );
    }

    @Transactional
    public Pair<Optional<ChannelReadResponse>, ChannelResponse> join(String inviteCode, Long userId) {
        Optional<ChannelReadResponse> channelByInviteCode = findChannelByInviteCode(inviteCode);
        if (channelByInviteCode.isEmpty()) {
            log.warn("[join] Channel not found with invite code: {}", inviteCode);
            return Pair.of(Optional.empty(), NOT_FOUND);
        }

        ChannelReadResponse channel = channelByInviteCode.get();

        if (isJoined(channel.channelId(), userId)) {
            log.warn("[join] User is already joined to channel: {}", channel.channelId());
            return Pair.of(Optional.empty(), ALREADY_JOINED);
        } else if (channel.headCount() >= MAX_CHANNEL_HEAD_COUNT) {
            log.warn("[join] Channel head count is over limit: {}", MAX_CHANNEL_HEAD_COUNT);
            return Pair.of(Optional.empty(), OVER_LIMIT);
        }

        Channel lockedChannel = channelRepository.findLockByChannelId(channel.channelId()).orElseThrow();

        if (lockedChannel.getHeadCount() < MAX_CHANNEL_HEAD_COUNT) {
            lockedChannel.changeHeadCount(lockedChannel.getHeadCount() + 1);
            userChannelRepository.save(UserChannel.create(userId, channel.channelId()));
        }
        return Pair.of(Optional.of(channel), SUCCESS);
    }

    @Transactional
    public ChannelResponse quit(Long channelId, Long userId) {
        if (!isJoined(channelId, userId)) {
            log.warn("[quit] User {} is not joined to channel: {}", userId, channelId);
            return NOT_JOINED;
        }

        Channel channel = channelRepository.findLockByChannelId(channelId).orElseThrow();

        if (channel.getHeadCount() > 0) {
            channel.changeHeadCount(channel.getHeadCount() - 1);
        } else {
            log.error("[quit] Channel ID: {} head count is 0. Cannot quit user ID: {} this channel.", channel, userId);
        }

        userChannelRepository.deleteByUserIdAndChannelId(userId, channelId);
        return SUCCESS;
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

    public boolean leave(Long userId) {
        return removeActiveChannel(userId);
    }

    public boolean isOnline(Long userId, Long channelId) {
        String activeChannel = redisTemplate.opsForValue().get(generateKey(userId));
        if (activeChannel == null) {
            return false;
        }
        return activeChannel.equals(channelId.toString());
    }

    public Optional<String> findInviteCode(Long channelId) {
        return channelRepository.findByChannelId(channelId)
                .map(Channel::getChannelInviteCode);
    }

    public List<Long> findOnlineParticipantIds(Long channelId) {
        List<Long> userIdsInChannel = findParticipantIds(channelId).stream()
                .map(ChannelParticipantResponse::userId)
                .toList();

        return findOnlineParticipantIds(channelId, userIdsInChannel);
    }

    public List<ChannelReadResponse> findChannelsByUserId(Long userId) {
        return userChannelRepository.findAllByUserId(userId).stream()
                .map(ChannelReadResponse::of)
                .toList();
    }

    public Optional<ChannelReadResponse> findChannelByInviteCode(String channelInviteCode) {
        return channelRepository.findByChannelInviteCode(channelInviteCode)
                .map(ChannelReadResponse::of);
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

    private boolean removeActiveChannel(Long userId) {
        return redisTemplate.delete(generateKey(userId));
    }

    private List<Long> findOnlineParticipantIds(Long channelId, List<Long> userIds) {
        List<String> keys = userIds.stream().map(this::generateKey).toList();
        List<String> channelIds = redisTemplate.opsForValue().multiGet(keys);
        if (channelIds == null) {
            return List.of();
        }

        List<Long> onlineUserIds = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            String chId = channelIds.get(i);
            if (chId != null && chId.equals(channelId.toString())) {
                onlineUserIds.add(userIds.get(i));
            }
        }

        return onlineUserIds;
    }

    private String generateKey(Long userId) {
        return "chat:user_id:%s:channel".formatted(userId);
    }
}
