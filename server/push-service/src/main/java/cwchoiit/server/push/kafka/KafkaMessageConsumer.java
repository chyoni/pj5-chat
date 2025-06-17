package cwchoiit.server.push.kafka;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.push.handler.RecordHandlerDispatcher;
import cwchoiit.server.push.messages.BaseRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageConsumer {

    private final RecordHandlerDispatcher recordHandlerDispatcher;

    @KafkaListener(
            topics = "${chat-push.kafka.listeners.push.topic}",
            groupId = "${chat-push.kafka.listeners.push.group-id}",
            concurrency = "${chat-push.kafka.listeners.push.concurrency}"
    )
    public void consumeMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.info("[consumeMessage] Received record = {}, from TOPIC = {}, on KEY = {}, PARTITION = {}, OFFSET = {}",
                    record.value(), record.topic(), record.key(), record.partition(), record.offset());

            Optional<BaseRecord> deserialized = Serializer.deserialize(record.value(), BaseRecord.class);

            deserialized.ifPresent(baseRecord ->
                    recordHandlerDispatcher.findHandler(baseRecord.type())
                            .ifPresent(handler -> handler.handle(baseRecord))
            );
        } catch (Exception e) {
            log.error("[consumeMessage] Record handling failed. cause : ", e);
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
