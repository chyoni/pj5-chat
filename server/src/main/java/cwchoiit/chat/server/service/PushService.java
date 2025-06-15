package cwchoiit.chat.server.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.chat.server.service.request.kafka.push.outbound.BaseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final KafkaProducerService kafkaProducerService;
    private final Map<String, Class<? extends BaseRecord>> pushMessageTypes = new HashMap<>();

    public void registerPushMessageType(String pushMessageType, Class<? extends BaseRecord> clazz) {
        pushMessageTypes.put(pushMessageType, clazz);
    }

    public void pushMessage(Long userId, String messageType, String message) {
        Optional.ofNullable(pushMessageTypes.get(messageType))
                .ifPresentOrElse(
                        clazz -> {
                            log.info("[pushMessage] Push message : {} to user : {}", message, userId);
                            Serializer.addKeyValue(message, "userId", String.valueOf(userId))
                                    .flatMap(json -> Serializer.deserialize(json, clazz))
                                    .ifPresent(kafkaProducerService::sendPushNotification);
                        },
                        () -> log.error("[pushMessage] Invalid push message type : {}", messageType)
                );
    }
}
