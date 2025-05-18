package cwchoiit.chat.server.service;

import cwchoiit.chat.server.EmbeddedRedis;
import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.constants.ChannelResponse;
import cwchoiit.chat.server.repository.UserChannelRepository;
import cwchoiit.chat.server.service.response.ChannelCreateResponse;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cwchoiit.chat.server.constants.ChannelResponse.INVALID_ARGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@Import(EmbeddedRedis.class)
@DisplayName("Service - ChannelService")
class ChannelServiceTest extends SpringBootTestConfiguration {

    @Autowired
    ChannelService channelService;

    @MockitoSpyBean
    StringRedisTemplate redisTemplate;

    @MockitoSpyBean
    UserChannelRepository userChannelRepository;

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
    @DisplayName("채널 생성에 성공한다.")
    void createDirectChannel_success() {
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
        Pair<Optional<ChannelCreateResponse>, ChannelResponse> createResult =
                channelService.createDirectChannel(1L, 2L, "Channel");

        ChannelCreateResponse channelCreateResponse = createResult.getFirst().orElseThrow();

        Pair<Optional<String>, ChannelResponse> enterResult =
                channelService.enter(1L, channelCreateResponse.channelId());

        String channelId = redisTemplate.opsForValue().get("chat:user_id:%s:channel".formatted(1));

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
                .expire(eq("chat:user_id:%s:channel".formatted(1)), anyLong(), eq(TimeUnit.SECONDS));
    }
}