package cwchoiit.chat.server.service;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service - CacheService")
class CacheServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @InjectMocks
    CacheService cacheService;

    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(CacheService.class);
    }

    @AfterEach
    void tearDown() {
        logCaptor.close();
    }

    @Test
    @DisplayName("단일 데이터 삭제 커맨드 요청 시, 예외가 발생하면 로그를 찍고 false를 반환한다.")
    void deleteException() {
        String deleteKey = "ex";
        doThrow(new RuntimeException("test exception"))
                .when(redisTemplate)
                .delete(deleteKey);

        boolean response = cacheService.delete(deleteKey);

        assertThat(response).isFalse();
        assertThat(logCaptor.getErrorLogs()).anyMatch(error -> error.contains("Redis delete failed"));
    }

    @Test
    @DisplayName("복수 데이터 삭제 커맨드 요청 시, 예외가 발생하면 로그를 찍고 false를 반환한다.")
    void deleteException2() {
        List<String> willDelete = List.of("A", "B", "C");
        doThrow(new RuntimeException("test exception"))
                .when(redisTemplate)
                .delete(willDelete);

        boolean response = cacheService.delete(willDelete);

        assertThat(response).isFalse();
        assertThat(logCaptor.getErrorLogs()).anyMatch(error -> error.contains("Redis delete failed"));
    }
}