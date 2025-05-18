package cwchoiit.chat.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@ToString
@Table(name = "channel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "channel_invite_code", nullable = false)
    private String channelInviteCode;

    @Column(name = "head_count", nullable = false)
    private int headCount;

    public static Channel create(String title, int headCount) {
        Channel channel = new Channel();
        channel.title = title;
        channel.headCount = headCount;
        channel.channelInviteCode = UUID.randomUUID().toString().replaceAll("-", "");
        return channel;
    }

    public static Channel createDirectChannel(String title) {
        Channel channel = new Channel();
        channel.title = title;
        channel.headCount = 2;
        channel.channelInviteCode = UUID.randomUUID().toString().replaceAll("-", "");
        return channel;
    }

    public void changeHeadCount(int count) {
        this.headCount = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return Objects.equals(channelId, channel.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channelId);
    }
}
