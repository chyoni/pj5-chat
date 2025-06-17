package cwchoiit.server.push.config;

import cwchoiit.server.push.kafka.KafkaConsumerAwareRebalanceListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory,
                                                                                                 KafkaConsumerAwareRebalanceListener kafkaConsumerAwareRebalanceListener) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setConsumerRebalanceListener(kafkaConsumerAwareRebalanceListener);

        log.info("[kafkaListenerContainerFactory] Kafka consumerFactory Set AckMode = {}", factory.getContainerProperties().getAckMode());
        return factory;
    }
}
