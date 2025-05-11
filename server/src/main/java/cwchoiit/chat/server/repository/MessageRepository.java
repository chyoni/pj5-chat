package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
