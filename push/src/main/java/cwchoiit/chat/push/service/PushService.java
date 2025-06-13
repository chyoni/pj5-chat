package cwchoiit.chat.push.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    public void pushMessage(Long userId, String messageType, String message) {
        log.info("[pushMessage] Push message : {} to user : {}", message, userId);
        // Implement Real World Push Alarm!
    }
}
