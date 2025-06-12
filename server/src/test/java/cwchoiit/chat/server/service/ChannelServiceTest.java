package cwchoiit.chat.server.service;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.constants.KeyPrefix;
import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.Channel;
import cwchoiit.chat.server.entity.UserChannel;
import cwchoiit.chat.server.repository.ChannelRepository;
import cwchoiit.chat.server.repository.UserChannelRepository;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
import cwchoiit.chat.server.service.response.ChannelParticipantResponse;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cwchoiit.chat.server.constants.ChannelResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@DisplayName("Service - ChannelService")
class ChannelServiceTest extends SpringBootTestConfiguration {

    @MockitoSpyBean
    UserConnectionService userConnectionService;
    @MockitoSpyBean
    StringRedisTemplate redisTemplate;
    @MockitoSpyBean
    UserChannelRepository userChannelRepository;
    @MockitoSpyBean
    ChannelRepository channelRepository;
    @Autowired
    ChannelService channelService;

    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(ChannelService.class);
    }

    @AfterEach
    void cleanUp() {
        logCaptor.close();
    }

    @Test
    @DisplayName("타이틀을 제공하지 않은 경우, 채널 생성에 실패한다.")
    void createDirectChannel() {
        // null
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> result =
                channelService.createDirectChannel(1L, 2L, null);

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Channel title is blank"));
        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo(INVALID_ARGS);

        // 빈 문자열
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> result2 =
                channelService.createDirectChannel(1L, 2L, "");

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Channel title is blank"));
        assertThat(result2.getFirst()).isEmpty();
        assertThat(result2.getSecond()).isEqualTo(INVALID_ARGS);
    }

    @Test
    @DisplayName("초대자와 연결된 사용자가 아닌 경우, 채널 생성에 실패한다.")
    void createDirectChannel_failed() {
        when(userConnectionService.findStatus(eq(1L), eq(2L)))
                .thenReturn(UserConnectionStatus.REJECTED);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> result =
                channelService.createDirectChannel(1L, 2L, "failed");

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Create direct channel failed. Creator and participant are not connected."));
        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo(NOT_ALLOWED);
    }

    @Test
    @DisplayName("채널 생성에 성공한다.")
    void createDirectChannel_success() {
        when(userConnectionService.findStatus(eq(1L), eq(2L)))
                .thenReturn(UserConnectionStatus.ACCEPTED);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> result =
                channelService.createDirectChannel(1L, 2L, "Channel");

        ChannelCreateResponse channelCreateResponse = result.getFirst().orElseThrow();

        assertThat(logCaptor.getWarnLogs()).isEmpty();
        assertThat(result.getFirst()).isNotEmpty();
        assertThat(channelCreateResponse).isNotNull();
        assertThat(channelCreateResponse.title()).isEqualTo("Channel");
        assertThat(channelCreateResponse.headCount()).isEqualTo(2);
        assertThat(result.getSecond()).isEqualTo(ChannelResponse.SUCCESS);

        verify(userChannelRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("그룹 채널 생성 시, 타이틀이 없는 경우 채널 생성에 실패한다.")
    void createGroupChannel() {
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> response = channelService.createGroupChannel(1L, List.of(2L, 3L), null);

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Channel title is blank"));
        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(INVALID_ARGS);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> response2 = channelService.createGroupChannel(1L, List.of(2L, 3L), "");

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Channel title is blank"));
        assertThat(response2.getFirst()).isEmpty();
        assertThat(response2.getSecond()).isEqualTo(INVALID_ARGS);
    }

    @Test
    @DisplayName("그룹 채널 생성 시, 최대 인원을 초과하면 채널 생성에 실패한다.")
    void createGroupChannel_failed() {
        List<Long> participantIds = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            participantIds.add((long) i);
        }
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> response =
                channelService.createGroupChannel(1L, participantIds, "Channel");

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("Channel head count is over limit"));
        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(OVER_LIMIT);
    }

    @Test
    @DisplayName("그룹 채널 생성 시, 채널 생성자와 참여자 간 ACCEPTED 상태가 맺어진 총 인원수와 파라미터로 받은 참여자 수가 다른 경우, 채널 생성에 실패한다.")
    void createGroupChannel_failed2() {
        long creatorId = 1L;
        List<Long> participants = List.of(2L, 3L);
        when(userConnectionService.countConnectionByStatus(creatorId, participants, UserConnectionStatus.ACCEPTED))
                .thenReturn(1L);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> response =
                channelService.createGroupChannel(creatorId, participants, "Channel");

        assertThat(logCaptor.getWarnLogs()).anyMatch(log -> log.contains("[createGroupChannel] Create group channel failed. Creator and participants are not connected."));
        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(NOT_ALLOWED);
    }

    @Test
    @DisplayName("그룹 채널 생성에 성공한다.")
    void createGroupChannel_success() {
        long creatorId = 1L;
        List<Long> participants = List.of(2L, 3L);
        when(userConnectionService.countConnectionByStatus(creatorId, participants, UserConnectionStatus.ACCEPTED))
                .thenReturn(2L);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> response =
                channelService.createGroupChannel(creatorId, participants, "Channel");

        verify(channelRepository, times(1)).save(any());
        verify(userChannelRepository, times(1)).saveAll(any());

        assertThat(response.getFirst()).isNotEmpty();
        assertThat(response.getFirst().get().title()).isEqualTo("Channel");
        assertThat(response.getFirst().get().headCount()).isEqualTo(3);
        assertThat(response.getSecond()).isEqualTo(ChannelResponse.SUCCESS);
    }

    @Test
    @DisplayName("채널 참여자가 아닌 경우, 채널 입장에 실패한다.")
    void enter() {
        Pair<Optional<String>, ChannelResponse> result = channelService.enter(1L, 2L);

        assertThat(logCaptor.getWarnLogs())
                .anyMatch(log -> log.equals("[enter] User is not joined to channel: 2"));
        assertThat(result.getFirst()).isEmpty();
        assertThat(result.getSecond()).isEqualTo(ChannelResponse.NOT_JOINED);
    }

    @Test
    @DisplayName("채널 ID로 채널을 찾지 못한 경우, 예외가 발생한다.")
    void enter2() {
        when(userChannelRepository.existsByUserIdAndChannelId(eq(1L), eq(2L)))
                .thenReturn(true);

        assertThatThrownBy(() -> channelService.enter(1L, 2L))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("채널 입장에 성공하면, 레디스에 채널 ID를 캐싱한다.")
    void enter3() {
        when(userConnectionService.findStatus(eq(1L), eq(2L)))
                .thenReturn(UserConnectionStatus.ACCEPTED);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> createResult =
                channelService.createDirectChannel(1L, 2L, "Channel");

        ChannelCreateResponse channelCreateResponse = createResult.getFirst().orElseThrow();

        Pair<Optional<String>, ChannelResponse> enterResult =
                channelService.enter(1L, channelCreateResponse.channelId());

        String channelId = redisTemplate.opsForValue().get("%s:%s:channel".formatted(KeyPrefix.USER_ID, 1));

        assertThat(channelId).isNotNull();
        assertThat(channelId).isEqualTo(channelCreateResponse.channelId().toString());
        assertThat(enterResult.getFirst()).isNotEmpty();
        assertThat(enterResult.getFirst().get()).isEqualTo(channelCreateResponse.title());
        assertThat(enterResult.getSecond()).isEqualTo(ChannelResponse.SUCCESS);
    }

    @Test
    @DisplayName("활성 채널에 TTL을 연장할 수 있다.")
    void refreshActiveChannel() {
        channelService.refreshActiveChannel(1L);

        verify(redisTemplate, times(1))
                .expire(eq("%s:%s:channel".formatted(KeyPrefix.USER_ID, 1)), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("유저의 활성 채널이 없는 경우, 온라인 상태를 false로 반환한다.")
    void isOnline() {
        boolean online = channelService.isOnline(100L, 1L);
        assertThat(online).isFalse();
    }

    @Test
    @DisplayName("유저의 활성 채널이 있는 경우, 온라인 상태를 true로 반환한다.")
    void isOnline2() {
        redisTemplate.opsForValue().set("%s:%s:channel".formatted(KeyPrefix.USER_ID, 1L), "1");
        boolean online = channelService.isOnline(1L, 1L);
        assertThat(online).isTrue();
    }

    @Test
    @DisplayName("채널 참여자가 없는 경우, 빈 리스트를 반환한다.")
    void findParticipantIds() {
        List<ChannelParticipantResponse> participantIds = channelService.findParticipantIds(100L);
        assertThat(participantIds).isEmpty();
    }

    @Test
    @DisplayName("채널 참여자가 있는 경우, 해당 참여자의 정보를 반환한다.")
    void findParticipantIds2() {
        when(userConnectionService.findStatus(eq(1L), eq(2L)))
                .thenReturn(UserConnectionStatus.ACCEPTED);

        Pair<Optional<ChannelCreateResponse>, ChannelResponse> channel =
                channelService.createDirectChannel(1L, 2L, "Channel");

        List<ChannelParticipantResponse> participantIds = channelService.findParticipantIds(channel.getFirst().orElseThrow().channelId());

        assertThat(participantIds).isNotEmpty();
        assertThat(participantIds.size()).isEqualTo(2);
        assertThat(participantIds)
                .extracting(ChannelParticipantResponse::userId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("채널 참여자 중, 온라인 유저들을 찾을 때, 채널 IDs가 null인 경우 빈 리스트를 반환한다.")
    void findOnlineParticipantIds() {
        long channelId = 100L;

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(anyCollection()))
                .thenReturn(null);

        List<Long> onlineParticipantIds = channelService.findOnlineParticipantIds(channelId, List.of());

        assertThat(onlineParticipantIds).isEmpty();
    }

    @Test
    @DisplayName("채널 참여자 중, 온라인 유저만 찾아와, 정상값을 반환한다.")
    void findOnlineParticipantIds2() {
        long channelId = 100L;

        List<String> values = new ArrayList<>();
        values.add(String.valueOf(channelId));
        values.add(null);
        values.add("200");

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        doReturn(valueOperations).when(redisTemplate).opsForValue();
        doReturn(values).when(valueOperations).multiGet(anyCollection());

        List<Long> onlineParticipantIds = channelService.findOnlineParticipantIds(channelId, List.of(1L, 2L, 3L));

        assertThat(onlineParticipantIds).isNotEmpty();
        assertThat(onlineParticipantIds).contains(1L);
        assertThat(onlineParticipantIds).containsExactlyInAnyOrder(1L, null, null);
    }

    @Test
    @DisplayName("채널 참여 코드를 찾을 수 있다.")
    void findInviteCode() {
        channelService.findInviteCode(100L);

        verify(channelRepository, times(1)).findByChannelId(eq(100L));
    }

    @Test
    @DisplayName("활성화된 채널 나가기 시, 관련 없는 유저 ID를 받으면 아무런 채널도 나가지 않는다.")
    void removeActiveChannel() {
        boolean result = channelService.leave(300L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("활성화된 채널 나가기 시, 정상 유저 ID를 받으면 해당 유저의 채널을 나간다.")
    void removeActiveChannel2() {

        channelService.setActiveChannel(1L, 100L);

        boolean result = channelService.leave(1L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유저 ID를 통해 해당 유저가 속한 채널들을 찾을 수 있다.")
    void findChannelsByUserId() {
        Channel ch1 = channelRepository.save(Channel.create("ch1", 2));
        Channel ch2 = channelRepository.save(Channel.create("ch2", 2));
        userChannelRepository.save(UserChannel.create(1L, ch1.getChannelId()));
        userChannelRepository.save(UserChannel.create(1L, ch2.getChannelId()));

        List<ChannelReadResponse> channels = channelService.findChannelsByUserId(1L);

        assertThat(channels).isNotEmpty();
        assertThat(channels).hasSize(2);
        assertThat(channels.get(0).channelId()).isEqualTo(ch1.getChannelId());
        assertThat(channels.get(1).channelId()).isEqualTo(ch2.getChannelId());
    }

    @Test
    @DisplayName("채널 초대 코드를 통해 채널을 찾을 수 있다.")
    void findChannelByInviteCode() {
        Channel channel = channelRepository.save(Channel.create("ch", 2));

        ChannelReadResponse channelReadResponse = channelService.findChannelByInviteCode(channel.getChannelInviteCode()).orElseThrow();

        assertThat(channelReadResponse.channelId()).isEqualTo(channel.getChannelId());
        assertThat(channelReadResponse.title()).isEqualTo(channel.getTitle());
        assertThat(channelReadResponse.headCount()).isEqualTo(channel.getHeadCount());
    }

    @Test
    @DisplayName("채널 삭제 시, 해당 채널에 삭제 요청을 한 유저가 포함되어 있지 않으면 삭제 되지 않는다.")
    void quit() {
        ChannelResponse response = channelService.quit(1L, 100L);
        assertThat(logCaptor.getWarnLogs())
                .anyMatch(log -> log.contains("User 100 is not joined to channel: 1"));

        assertThat(response).isEqualTo(ChannelResponse.NOT_JOINED);
    }

    @Test
    @DisplayName("채널 삭제 시, 채널 ID로 채널을 찾지 못한 경우 예외가 발생한다.")
    void quit2() {
        long userId = 1L;
        long channelId = 100L;
        when(userChannelRepository.existsByUserIdAndChannelId(eq(userId), eq(channelId)))
                .thenReturn(true);

        assertThatThrownBy(() -> channelService.quit(channelId, userId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("채널 삭제 시, 채널의 head count 가 0인 경우, 채널의 유저수를 제거하지 못한다.")
    void quit3() {
        long userId = 1L;
        long channelId = 100L;
        when(userChannelRepository.existsByUserIdAndChannelId(eq(userId), eq(channelId)))
                .thenReturn(true);
        when(channelRepository.findLockByChannelId(eq(channelId)))
                .thenReturn(Optional.of(Channel.create("ch", 0)));

        ChannelResponse response = channelService.quit(channelId, userId);

        verify(userChannelRepository, times(1)).deleteByUserIdAndChannelId(eq(userId), eq(channelId));
        assertThat(response).isEqualTo(SUCCESS);
    }

    @Test
    @DisplayName("채널 삭제 시, 모든 상태가 정상인 경우, 채널의 유저수를 하나 줄이고 성공 응답을 반환한다.")
    void quit4() {
        Channel ch = Channel.create("ch", 2);
        channelRepository.save(ch);

        long userId = 1L;
        userChannelRepository.save(UserChannel.create(userId, ch.getChannelId()));

        ChannelResponse response = channelService.quit(ch.getChannelId(), userId);

        verify(userChannelRepository, times(1)).deleteByUserIdAndChannelId(eq(userId), eq(ch.getChannelId()));
        assertThat(ch.getHeadCount()).isEqualTo(1);
        assertThat(response).isEqualTo(SUCCESS);
    }

    @Test
    @DisplayName("채널 참여 시, 채널 초대 코드를 통해 채널을 찾지 못한 경우, 참여에 실패한다.")
    void join() {
        Pair<Optional<ChannelReadResponse>, ChannelResponse> response = channelService.join("1234", 1L);

        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(ChannelResponse.NOT_FOUND);
        assertThat(logCaptor.getWarnLogs())
                .anyMatch(log -> log.contains("Channel not found with invite code: 1234"));
    }

    @Test
    @DisplayName("채널 참여 시, 해당 유저가 이미 채널에 참여된 경우, 채널 참여에 실패한다.")
    void join2() {
        long userId = 1L;
        Channel ch = channelRepository.save(Channel.create("ch", 2));
        userChannelRepository.save(UserChannel.create(userId, ch.getChannelId()));

        Pair<Optional<ChannelReadResponse>, ChannelResponse> response = channelService.join(ch.getChannelInviteCode(), userId);

        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(ChannelResponse.ALREADY_JOINED);
        assertThat(logCaptor.getWarnLogs())
                .anyMatch(log -> log.contains("User is already joined to channel: %d".formatted(ch.getChannelId())));
    }

    @Test
    @DisplayName("채널 참여 시, 채널의 최대 허용 인원이 초과한 경우, 채널 참여에 실패한다.")
    void join3() {
        long userId = 1L;
        Channel ch = channelRepository.save(Channel.create("ch", 101));
        userChannelRepository.save(UserChannel.create(userId, ch.getChannelId()));

        Pair<Optional<ChannelReadResponse>, ChannelResponse> response = channelService.join(ch.getChannelInviteCode(), 2L);

        assertThat(response.getFirst()).isEmpty();
        assertThat(response.getSecond()).isEqualTo(ChannelResponse.OVER_LIMIT);
        assertThat(logCaptor.getWarnLogs())
                .anyMatch(log -> log.contains("Channel head count is over limit"));
    }

    @Test
    @DisplayName("채널 참여 시, 스레드 경합에 실패해 다른 스레드에 의해 마지막 허용 인원을 채운 경우, 채널에 참여하지 못한다.")
    void join4() {
        long userId = 1L;
        Channel ch = channelRepository.save(Channel.create("ch", 2));
        userChannelRepository.save(UserChannel.create(userId, ch.getChannelId()));

        Channel channel = Channel.create(ch.getTitle(), 101);
        when(channelRepository.findLockByChannelId(eq(ch.getChannelId())))
                .thenReturn(Optional.of(channel));

        Pair<Optional<ChannelReadResponse>, ChannelResponse> response = channelService.join(ch.getChannelInviteCode(), 2L);

        assertThat(channel.getHeadCount()).isEqualTo(101);
        assertThat(response.getSecond()).isEqualTo(SUCCESS);
    }

    @Test
    @DisplayName("채널 참여 시, 모든 조건을 만족하는 경우, 채널 참여에 성공한다.")
    void join5() {
        long userId = 1L;
        Channel ch = channelRepository.save(Channel.create("ch", 2));
        userChannelRepository.save(UserChannel.create(userId, ch.getChannelId()));

        when(channelRepository.findLockByChannelId(eq(ch.getChannelId())))
                .thenReturn(Optional.of(ch));

        Pair<Optional<ChannelReadResponse>, ChannelResponse> response = channelService.join(ch.getChannelInviteCode(), 2L);

        assertThat(response.getFirst()).isNotEmpty();
        assertThat(response.getFirst().get().channelId()).isEqualTo(ch.getChannelId());
        assertThat(response.getFirst().get().title()).isEqualTo(ch.getTitle());
        assertThat(response.getFirst().get().headCount()).isEqualTo(2);
        assertThat(ch.getHeadCount()).isEqualTo(3);
        assertThat(response.getSecond()).isEqualTo(ChannelResponse.SUCCESS);
    }
}