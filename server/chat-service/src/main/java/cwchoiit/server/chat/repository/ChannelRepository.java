package cwchoiit.server.chat.repository;

import cwchoiit.server.chat.entity.Channel;
import cwchoiit.server.chat.repository.projection.ChannelInformation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByChannelId(Long channelId);

    Optional<ChannelInformation> findByChannelInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Channel> findLockByChannelId(Long channelId);
}
