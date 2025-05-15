package cwchoiit.chat.server.entity;

import cwchoiit.chat.server.SpringBootTestConfiguration;
import cwchoiit.chat.server.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@DisplayName("Entity - User")
class UserTest extends SpringBootTestConfiguration {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("유저 생성 팩토리 메서드 검증")
    void create() {
        User user = User.create("test", "test");

        assertThat(user.getUsername()).isEqualTo("test");
        assertThat(user.getPassword()).isEqualTo("test");
        assertThat(user.getConnectionCount()).isEqualTo(0);
        assertThat(user.getConnectionInviteCode()).isNotNull();
    }

    @Test
    @DisplayName("데아터베이스에 유저 저장 시, createdAt, updatedAt 자동 생성")
    void save() {
        User user = User.create("test", "test");
        User savedUser = userRepository.save(user);

        assertThat(user.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("연결 횟수 변경 시, 변경 내용이 정상적으로 저장된다.")
    void updateConnectionCount() {
        User user = User.create("test", "test");

        assertThat(user.getConnectionCount()).isEqualTo(0);

        user.changeConnectionCount(1);

        assertThat(user.getConnectionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("유저 객체는 유저명으로 동등 비교를 한다.")
    void equals() {
        User user1 = User.create("test", "test1");
        User user2 = User.create("test", "test2");

        assertThat(user1).isEqualTo(user2);
    }
}