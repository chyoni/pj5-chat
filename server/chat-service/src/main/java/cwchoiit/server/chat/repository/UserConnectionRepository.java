package cwchoiit.server.chat.repository;

import cwchoiit.server.chat.entity.UserConnection;
import cwchoiit.server.chat.repository.projection.ConnectionInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
                                                  @Param("status") String status);

    @Query(
            nativeQuery = true,
            value = "select count(*) " +
                    "from user_connection uc " +
                    "where uc.partner_a_user_id = :partnerAUserId " +
                    "and uc.partner_b_user_id in (:partnerBUserIds) " +
                    "and uc.status = :status"
    )
    long countByPartnerAUserIdAndPartnerBUserIdInAndStatus(@Param("partnerAUserId") Long partnerAUserId,
                                                           @Param("partnerBUserIds") List<Long> partnerBUserIds,
                                                           @Param("status") String status);

    @Query(
            nativeQuery = true,
            value = "select count(*) " +
                    "from user_connection uc " +
                    "where uc.partner_b_user_id = :partnerBUserId " +
                    "and uc.partner_a_user_id in (:partnerAUserIds) " +
                    "and uc.status = :status"
    )
    long countByPartnerBUserIdAndPartnerAUserIdInAndStatus(@Param("partnerBUserId") Long partnerBUserId,
                                                           @Param("partnerAUserIds") List<Long> partnerAUserIds,
                                                           @Param("status") String status);

    @Query(
            nativeQuery = true,
            value = "select uc.partner_b_user_id as userId, u.username as username, uc.inviter_user_id as inviterUserId " +
                    "from user_connection uc " +
                    "join user u on uc.partner_b_user_id = u.user_id " +
                    "where uc.partner_a_user_id = :userId " +
                    "and uc.status = :status"
    )
    List<ConnectionInformation> findAllUserConnectionByPartnerAUserId(@Param("userId") Long userId,
                                                                      @Param("status") String status);

    @Query(
            nativeQuery = true,
            value = "select uc.partner_a_user_id as userId, u.username as username, uc.inviter_user_id as inviterUserId " +
                    "from user_connection uc " +
                    "join user u on uc.partner_a_user_id = u.user_id " +
                    "where uc.partner_b_user_id = :userId " +
                    "and uc.status = :status"
    )
    List<ConnectionInformation> findAllUserConnectionByPartnerBUserId(@Param("userId") Long userId,
                                                                      @Param("status") String status);
}
