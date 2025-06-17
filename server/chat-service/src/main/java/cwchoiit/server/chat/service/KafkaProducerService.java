package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.service.request.kafka.push.outbound.BaseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${chat-server.kafka.listeners.push.topic}")
    private String topic;

    public void sendPushNotification(BaseRecord record) {
        Serializer.serialize(record)
                .ifPresent(serializedRecord -> kafkaTemplate.send(topic, serializedRecord));
    }
}
