package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.constants.ChannelResponse;
import cwchoiit.server.chat.entity.Channel;
import cwchoiit.server.chat.entity.UserChannel;
import cwchoiit.server.chat.repository.ChannelRepository;
import cwchoiit.server.chat.repository.UserChannelRepository;
import cwchoiit.server.chat.service.response.ChannelCreateResponse;
import cwchoiit.server.chat.service.response.ChannelParticipantResponse;
import cwchoiit.server.chat.service.response.ChannelReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static cwchoiit.server.chat.constants.ChannelResponse.*;
import static cwchoiit.server.chat.constants.KeyPrefix.*;
import static cwchoiit.server.chat.constants.UserConnectionStatus.ACCEPTED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {

    private final UserChannelRepository userChannelRepository;
    private final UserConnectionService userConnectionService;
    private final ChannelRepository channelRepository;
    private final CacheService cacheService;

    private final long TIME_TO_LIVE = 300L;
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
            cacheService.delete(List.of(
                    cacheService.generateKey(CHANNEL, inviteCode),
                    cacheService.generateKey(CHANNEL, String.valueOf(userId))
            ));
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
        cacheService.delete(List.of(
                cacheService.generateKey(CHANNEL, channel.getChannelInviteCode()),
                cacheService.generateKey(CHANNEL, String.valueOf(userId))
        ));
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
        String key = cacheService.generateKey(JOINED_CHANNEL, String.valueOf(channelId), String.valueOf(userId));

        return cacheService.get(key)
                .map(v -> true)
                .orElseGet(() -> {
                    boolean fromDatabase = userChannelRepository.existsByUserIdAndChannelId(userId, channelId);
                    if (fromDatabase) {
                        cacheService.set(key, "OK", TIME_TO_LIVE);
                    }
                    return fromDatabase;
                });
    }

    public boolean leave(Long userId) {
        return removeActiveChannel(userId);
    }

    public boolean isOnline(Long userId, Long channelId) {
        String activeChannel = cacheService.get(generateKey(userId)).orElse(null);
        if (activeChannel == null) {
            return false;
        }
        return activeChannel.equals(channelId.toString());
    }

    public Optional<String> findInviteCode(Long channelId) {
        String key = cacheService.generateKey(CHANNEL_INVITECODE, String.valueOf(channelId));

        return cacheService.get(key)
                .or(() -> {
                    Optional<String> inviteCode = channelRepository
                            .findByChannelId(channelId)
                            .map(Channel::getChannelInviteCode);

                    inviteCode.ifPresent(code -> cacheService.set(key, code, TIME_TO_LIVE));
                    return inviteCode;
                });
    }

    public List<Long> findOnlineParticipantIds(Long channelId, List<Long> userIds) {
        List<String> keys = userIds.stream().map(this::generateKey).toList();
        List<String> channelIds = cacheService.get(keys);
        if (channelIds == null) {
            return List.of();
        }

        List<Long> onlineUserIds = new ArrayList<>(userIds.size());
        for (int i = 0; i < userIds.size(); i++) {
            String chId = channelIds.get(i);
            onlineUserIds.add(chId != null && chId.equals(channelId.toString()) ?
                    userIds.get(i) :
                    null
            );
        }

        return onlineUserIds;
    }

    public List<ChannelReadResponse> findChannelsByUserId(Long userId) {
        String key = cacheService.generateKey(CHANNELS, String.valueOf(userId));

        return cacheService.get(key)
                .map(cached -> Serializer.deserializeList(cached, ChannelReadResponse.class))
                .orElseGet(() -> {
                    List<ChannelReadResponse> fromDatabase = userChannelRepository.findAllByUserId(userId).stream()
                            .map(ChannelReadResponse::of)
                            .toList();

                    if (!fromDatabase.isEmpty()) {
                        Serializer.serialize(fromDatabase)
                                .ifPresent(serialized -> cacheService.set(key, serialized, TIME_TO_LIVE));
                    }
                    return fromDatabase;
                });
    }

    public Optional<ChannelReadResponse> findChannelByInviteCode(String channelInviteCode) {
        String key = cacheService.generateKey(CHANNEL, channelInviteCode);

        return cacheService.get(key)
                .flatMap(cached -> Serializer.deserialize(cached, ChannelReadResponse.class))
                .or(() -> {
                    Optional<ChannelReadResponse> channelReadResponse = channelRepository
                            .findByChannelInviteCode(channelInviteCode)
                            .map(ChannelReadResponse::of);

                    channelReadResponse.flatMap(Serializer::serialize)
                            .ifPresent(serialized -> cacheService.set(key, serialized, TIME_TO_LIVE));

                    return channelReadResponse;
                });
    }

    public List<ChannelParticipantResponse> findParticipantIds(Long channelId) {
        String key = cacheService.generateKey(PARTICIPANT_IDS, String.valueOf(channelId));

        return cacheService.get(key)
                .map(cached -> Serializer.deserializeList(cached, String.class).stream()
                        .map(id -> new ChannelParticipantResponse(Long.valueOf(id)))
                        .toList()
                ).orElseGet(() -> {
                    List<ChannelParticipantResponse> fromDatabase = userChannelRepository
                            .findUserIdsByChannelId(channelId).stream()
                            .map(ChannelParticipantResponse::of)
                            .toList();

                    if (!fromDatabase.isEmpty()) {
                        List<String> userIds = fromDatabase.stream()
                                .map(p -> p.userId().toString())
                                .toList();

                        Serializer.serialize(userIds)
                                .ifPresent(json -> cacheService.set(key, json, TIME_TO_LIVE));
                    }

                    return fromDatabase;
                });
    }

    public void setActiveChannel(Long userId, Long channelId) {
        cacheService.set(generateKey(userId), channelId.toString(), TIME_TO_LIVE);
    }

    public void refreshActiveChannel(Long userId) {
        cacheService.expire(generateKey(userId), TIME_TO_LIVE);
    }

    private boolean removeActiveChannel(Long userId) {
        return cacheService.delete(generateKey(userId));
    }

    private String generateKey(Long userId) {
        return "%s:%s:channel".formatted(USER_ID, userId);
    }
}
