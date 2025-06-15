package cwchoiit.chat.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
public class KafkaConfig {

    /**
     * 카프카 프로듀서는 실제로 카프카로 메시지를 전송하기 전까지 초기화를 뒤로 미루는 Lazy가 기본값이다.
     * 이 경우, 트래픽이 매우 많이 들어오는 최악의 상황에서는 첫 메시지를 보낼때 초기화가 늦어지면 메시지 전송에 실패할 수 있기 때문에
     * 서버를 띄우자마자 프로듀서 초기화를 바로 해버리는 작업을 진행한다.
     *
     * @param producerFactory {@link DefaultKafkaProducerFactory}
     * @return {@link KafkaTemplate}
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(DefaultKafkaProducerFactory<String, String> producerFactory) {
        Producer<String, String> producer = producerFactory.createProducer();
        producer.close();

        log.info("[kafkaTemplate] Producer created.");
        return new KafkaTemplate<>(producerFactory);
    }
}
