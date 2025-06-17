package cwchoiit.server.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
@ToString
@Table(name = "user_channel")
@IdClass(UserChannel.UserChannelId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChannel extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "last_read_msg_seq", nullable = false)
    private long lastReadMessageSequence;

    public static UserChannel create(Long userId, Long channelId) {
        UserChannel userChannel = new UserChannel();
        userChannel.userId = userId;
        userChannel.channelId = channelId;
        userChannel.lastReadMessageSequence = 0;
        return userChannel;
    }

    public void changeLastReadMessageSequence(long sequence) {
        this.lastReadMessageSequence = sequence;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserChannelId implements Serializable {
        private Long userId;
        private Long channelId;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            UserChannelId that = (UserChannelId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(channelId, that.channelId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, channelId);
        }
    }
}
