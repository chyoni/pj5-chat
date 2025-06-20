package cwchoiit.server.chat.entity;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@DisplayName("Entity - Message")
class MessageTest extends SpringBootTestConfiguration {

    @Autowired
    MessageRepository messageRepository;

    @Test
    @DisplayName("메시지 생성 팩토리 메서드 검증")
    void create() {
        Message message = Message.create(1L, "test");

        assertThat(message.getUserId()).isEqualTo(1);
        assertThat(message.getContent()).isEqualTo("test");
        assertThat(message.getCreatedAt()).isNull();
        assertThat(message.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("메시지가 생성되면, createdAt, updatedAt 자동 생성")
    void save() {
        Message message = Message.create(1L, "test");

        Message saved = messageRepository.save(message);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(message.getUserId());
        assertThat(saved.getContent()).isEqualTo(message.getContent());
    }

    @Test
    @DisplayName("메시지는 Sequence 값으로 동등 비교를 한다.")
    void equals() {
        Message message1 = Message.create(1L, "test1");
        Message message2 = Message.create(1L, "test2");

        assertThat(message1).isEqualTo(message2); // 둘 다 아직 sequence 값이 null 일것이므로

        Message saved = messageRepository.save(message1);

        assertThat(saved).isEqualTo(message1);
    }
}