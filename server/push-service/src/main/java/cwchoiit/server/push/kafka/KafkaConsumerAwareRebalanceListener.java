package cwchoiit.server.push.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class KafkaConsumerAwareRebalanceListener implements ConsumerAwareRebalanceListener {

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        log.info("[onPartitionsAssigned] Kafka consumer: {} assigned: {}", consumer.toString(), partitions.toString());
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.info("[onPartitionsRevoked]  Kafka consumer revoked: {}", partitions.toString());
    }
}
