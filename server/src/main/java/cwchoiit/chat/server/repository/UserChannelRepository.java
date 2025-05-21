package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.UserChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChannelRepository extends JpaRepository<UserChannel, UserChannel.UserChannelId> {
    boolean existsByUserIdAndChannelId(Long userId, Long channelId);

    List<UserChannel> findUserIdsByChannelId(Long channelId);
}
