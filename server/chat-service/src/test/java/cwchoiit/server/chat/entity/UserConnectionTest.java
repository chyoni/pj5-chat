package cwchoiit.server.chat.entity;

import cwchoiit.server.chat.SpringBootTestConfiguration;
import cwchoiit.server.chat.constants.UserConnectionStatus;
import cwchoiit.server.chat.repository.UserConnectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@DisplayName("Entity - UserConnection")
class UserConnectionTest extends SpringBootTestConfiguration {

    @Autowired
    UserConnectionRepository userConnectionRepository;

    @Test
    @DisplayName("유저 커넥션 생성 팩토리 메서드 검증")
    void create() {
        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, UserConnectionStatus.PENDING);

        assertThat(userConnection.getPartnerAUserId()).isEqualTo(1L);
        assertThat(userConnection.getPartnerBUserId()).isEqualTo(2L);
        assertThat(userConnection.getInviterUserId()).isEqualTo(1L);
        assertThat(userConnection.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
    }

    @Test
    @DisplayName("유저 커넥션이 저장되면, createdAt, updatedAt 자동 생성")
    void save() {
        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, UserConnectionStatus.PENDING);
        UserConnection savedUserConnection = userConnectionRepository.save(userConnection);

        assertThat(savedUserConnection.getCreatedAt()).isNotNull();
        assertThat(savedUserConnection.getUpdatedAt()).isNotNull();
        assertThat(savedUserConnection.getPartnerAUserId()).isEqualTo(userConnection.getPartnerAUserId());
        assertThat(savedUserConnection.getPartnerBUserId()).isEqualTo(userConnection.getPartnerBUserId());
        assertThat(savedUserConnection.getInviterUserId()).isEqualTo(userConnection.getInviterUserId());
    }

    @Test
    @DisplayName("상태 변경 메서드로 상태를 변경하면, 상태가 정상적으로 변경된다.")
    void changeStatus() {
        UserConnection userConnection = UserConnection.create(1L, 2L, 1L, UserConnectionStatus.PENDING);
        assertThat(userConnection.getStatus()).isEqualTo(UserConnectionStatus.PENDING);

        userConnection.changeStatus(UserConnectionStatus.ACCEPTED);
        assertThat(userConnection.getStatus()).isEqualTo(UserConnectionStatus.ACCEPTED);
    }

    @Test
    @DisplayName("유저 커넥션은 Partner A, B의 ID로 동등 비교를 한다.")
    void equals() {
        UserConnection userConnection1 = UserConnection.create(1L, 2L, 4L, UserConnectionStatus.PENDING);
        UserConnection userConnection2 = UserConnection.create(1L, 2L, 3L, UserConnectionStatus.NONE);

        assertThat(userConnection1).isEqualTo(userConnection2);
    }
}