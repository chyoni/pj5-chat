package cwchoiit.chat.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final Set<String> pushMessageTypes = new HashSet<>();

    public void registerPushMessageType(String pushMessageType) {
        pushMessageTypes.add(pushMessageType);
    }

    public void pushMessage(Long userId, String messageType, String message) {
        if (pushMessageTypes.contains(messageType)) {
            log.info("[pushMessage] Push message : {} to user : {}", message, userId);
            // Implement Real World Push Alarm!
        }
    }
}
