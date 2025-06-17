package cwchoiit.server.chat.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service - SessionService")
class SessionServiceTest {

    @Mock
    SessionRepository<Session> sessionRepository;

    @InjectMocks
    SessionService sessionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("TTL을 갱신할 때, 세션 레포지토리에서 세션을 찾으면, TTL 갱신을 성공한다.")
    void refreshTimeToLive() {
        String sessionId = "any";

        Session mockSession = mock(Session.class);
        when(sessionRepository.findById(sessionId)).thenReturn(mockSession);

        sessionService.refreshTimeToLive(sessionId);

        verify(mockSession, times(1)).setLastAccessedTime(any());
    }

    @Test
    @DisplayName("세션이 존재하지 않으면 TTL 갱신은 발생하지 않는다.")
    void refreshTimeToLive_nullSession() {
        String sessionId = "not-found";

        when(sessionRepository.findById(sessionId)).thenReturn(null);

        sessionService.refreshTimeToLive(sessionId);

        // 모킹된 session 객체 없으니 따로 verify 필요 없음 = 예외 안 나면 통과
    }

    @Test
    @DisplayName("로그인 한 유저 이름을 찾을 때, SecurityContextHolder와 상호작용한다.")
    void findUsername() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test");

        String username = sessionService.findUsername();

        assertThat(username).isEqualTo("test");
        verify(authentication, times(1)).getName();
    }
}