package cwchoiit.server.chat.entity;

import cwchoiit.server.chat.constants.UserConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Entity
@ToString
@Table(name = "user_connection")
@IdClass(UserConnection.UserConnectionId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConnection extends BaseEntity {

    @Id
    @Column(name = "partner_a_user_id", nullable = false)
    private Long partnerAUserId;

    @Id
    @Column(name = "partner_b_user_id", nullable = false)
    private Long partnerBUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserConnectionStatus status;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    public static UserConnection create(Long partnerAUserId,
                                        Long partnerBUserId,
                                        Long inviterUserId,
                                        UserConnectionStatus status) {
        UserConnection userConnection = new UserConnection();
        userConnection.partnerAUserId = partnerAUserId;
        userConnection.partnerBUserId = partnerBUserId;
        userConnection.inviterUserId = inviterUserId;
        userConnection.status = status;
        return userConnection;
    }

    public void changeStatus(UserConnectionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserConnection that = (UserConnection) o;
        return Objects.equals(partnerAUserId, that.partnerAUserId) && Objects.equals(partnerBUserId, that.partnerBUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partnerAUserId, partnerBUserId);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserConnectionId implements Serializable {
        private Long partnerAUserId;
        private Long partnerBUserId;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            UserConnectionId that = (UserConnectionId) o;
            return Objects.equals(partnerAUserId, that.partnerAUserId) && Objects.equals(partnerBUserId, that.partnerBUserId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partnerAUserId, partnerBUserId);
        }
    }
}
