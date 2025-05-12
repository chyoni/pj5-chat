package cwchoiit.chat.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Getter
@Entity
@ToString
@Table(name = "message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence", nullable = false, unique = true)
    private Long messageSequence;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "content", nullable = false)
    private String content;

    public static Message create(String username, String content) {
        Message message = new Message();
        message.username = username;
        message.content = content;
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageSequence, message.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageSequence);
    }
}
