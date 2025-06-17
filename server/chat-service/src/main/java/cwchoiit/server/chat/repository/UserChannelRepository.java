package cwchoiit.server.chat.repository;

import cwchoiit.server.chat.entity.UserChannel;
import cwchoiit.server.chat.repository.projection.ChannelInformation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserChannelRepository extends JpaRepository<UserChannel, UserChannel.UserChannelId> {
    boolean existsByUserIdAndChannelId(Long userId, Long channelId);

    List<UserChannel> findUserIdsByChannelId(Long channelId);

    @Query(
            nativeQuery = true,
            value = "select c.channel_id as channelId, c.title as title, c.head_count as headCount " +
                    "from user_channel uc " +
                    "join channel c on uc.channel_id = c.channel_id " +
                    "where uc.user_id = :userId"
    )
    List<ChannelInformation> findAllByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndChannelId(Long userId, Long channelId);
}
