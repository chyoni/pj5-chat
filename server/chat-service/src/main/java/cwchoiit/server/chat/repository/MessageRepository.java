package cwchoiit.server.chat.repository;

import cwchoiit.server.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
