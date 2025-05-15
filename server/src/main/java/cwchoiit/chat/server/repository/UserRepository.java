package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);

    Optional<User> findByConnectionInviteCode(String connectionInviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findLockByUserId(Long userId);
}
