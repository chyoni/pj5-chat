package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.UserChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChannelRepository extends JpaRepository<UserChannel, UserChannel.UserChannelId> {
    boolean existsByUserIdAndChannelId(Long userId, Long channelId);
}
