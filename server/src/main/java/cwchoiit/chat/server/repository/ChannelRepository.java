package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByChannelId(Long channelId);
}
