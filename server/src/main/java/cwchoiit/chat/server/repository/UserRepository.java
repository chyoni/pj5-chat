package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);

    Optional<User> findByConnectionInviteCode(String connectionInviteCode);
}
