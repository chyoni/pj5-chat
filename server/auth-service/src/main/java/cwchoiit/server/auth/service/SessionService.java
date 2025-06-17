package cwchoiit.server.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> sessionRepository;

    public void refreshTimeToLive(String sessionId) {
        Session httpSession = sessionRepository.findById(sessionId);
        if (httpSession != null) {
            httpSession.setLastAccessedTime(Instant.now());
        }
    }

    public String findUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
