package cwchoiit.chat.server.repository;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, UserConnection.UserConnectionId> {

    @Query(
            nativeQuery = true,
            value = "select uc.partner_a_user_id, uc.partner_b_user_id, uc.inviter_user_id, uc.status, uc.created_at, uc.updated_at " +
                    "from user_connection uc " +
                    "where uc.partner_a_user_id = :partnerAUserId " +
                    "and uc.partner_b_user_id = :partnerBUserId"
    )
    Optional<UserConnection> findUserConnectionBy(@Param("partnerAUserId") Long partnerAUserId,
                                                  @Param("partnerBUserId") Long partnerBUserId);

    @Query(
            nativeQuery = true,
            value = "select uc.partner_a_user_id, uc.partner_b_user_id, uc.inviter_user_id, uc.status, uc.created_at, uc.updated_at " +
                    "from user_connection uc " +
                    "where uc.partner_a_user_id = :partnerAUserId " +
                    "and uc.partner_b_user_id = :partnerBUserId " +
                    "and uc.status = :status"
    )
    Optional<UserConnection> findUserConnectionBy(@Param("partnerAUserId") Long partnerAUserId,
                                                  @Param("partnerBUserId") Long partnerBUserId,
                                                  @Param("status") UserConnectionStatus status);
}
