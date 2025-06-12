package cwchoiit.chat.server.service;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.entity.UserConnection;
import cwchoiit.chat.server.repository.UserConnectionRepository;
import cwchoiit.chat.server.repository.UserRepository;
import cwchoiit.chat.server.repository.projection.ConnectionInformation;
import cwchoiit.chat.server.service.response.UserReadResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static cwchoiit.chat.common.serializer.Serializer.deserializeList;
import static cwchoiit.chat.common.serializer.Serializer.serialize;
import static cwchoiit.chat.server.constants.KeyPrefix.*;
import static cwchoiit.chat.server.constants.UserConnectionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserConnectionService {

    private final UserService userService;
    private final UserConnectionRepository userConnectionRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final long TIME_TO_LIVE = 600L;
    @Getter
    @Setter
    private int LIMIT_CONNECTION_COUNT = 1_000;

    @Transactional
    public Pair<Optional<Long>, String> invite(Long inviterUserId, String inviteCode) {
        UserReadResponse partner = userService.findUserByConnectionInviteCode(inviteCode).orElse(null);
        if (partner == null) {
            log.error("[invite] partner not found with invite code: {}", inviteCode);
            return Pair.of(Optional.empty(), "partner not found with invite code: " + inviteCode);
        }
        if (partner.userId().equals(inviterUserId)) {
            log.error("[invite] Cannot invite self.");
            return Pair.of(Optional.empty(), "Cannot invite self.");
        }

        UserConnectionStatus status = findStatus(inviterUserId, partner.userId());
        return switch (status) {
            case NONE, DISCONNECTED -> {
                if (userService.findConnectionCountByUserId(inviterUserId).orElse(0) >= LIMIT_CONNECTION_COUNT) {
                    yield Pair.of(Optional.empty(), "Connection count limit exceeded.");
                }
                String inviterUsername = userService.findUsernameByUserId(inviterUserId).orElse(null);
                if (inviterUsername == null) {
                    log.error("[invite] inviter not found: {}", inviterUserId);
                    yield Pair.of(Optional.empty(), "inviter not found: " + inviterUserId);
                }
                createConnection(inviterUserId, partner.userId());
                yield Pair.of(Optional.of(partner.userId()), inviterUsername);
            }
            case ACCEPTED -> Pair.of(Optional.empty(), "Already invited.");
            case PENDING, REJECTED -> Pair.of(Optional.empty(), "Already invited or rejected.");
        };
    }

    @Transactional
    public Pair<Optional<Long>, String> accept(Long acceptorUserId, String inviterUsername) {
        Long inviterUserId = userService.findUserIdByUsername(inviterUsername).orElseThrow();

        if (inviterUserId.equals(acceptorUserId)) {
            log.error("[accept] Cannot accept self.");
            return Pair.of(Optional.empty(), "Cannot accept self.");
        }

        if (!findInviterUserId(inviterUserId, acceptorUserId).equals(inviterUserId)) {
            log.error("[accept] Invalid inviter's connection.");
            return Pair.of(Optional.empty(), "Invalid inviter's connection.");
        }

        UserConnectionStatus status = findStatus(inviterUserId, acceptorUserId);

        if (status == ACCEPTED) {
            return Pair.of(Optional.empty(), "Already accepted.");
        }

        if (status != PENDING) {
            log.error("[accept] Invalid status: {}", status);
            return Pair.of(Optional.empty(), "Invalid status: " + status);
        }

        String acceptorUsername = userService.findUsernameByUserId(acceptorUserId).orElseThrow();
        accept(inviterUserId, acceptorUserId);

        return Pair.of(Optional.of(inviterUserId), acceptorUsername);
    }

    @Transactional
    public Pair<Boolean, String> reject(Long declinerId, String inviterUsername) {
        Long inviterUserId = userService.findUserIdByUsername(inviterUsername).orElseThrow();

        if (inviterUserId.equals(declinerId)) {
            log.error("[reject] Invalid inviter's connection.");
            return Pair.of(false, "Invalid inviter's connection.");
        }

        if (!findInviterUserId(inviterUserId, declinerId).equals(inviterUserId)) {
            log.error("[reject] Invalid inviter's connection.");
            return Pair.of(false, "Invalid inviter's connection.");
        }

        UserConnectionStatus status = findStatus(inviterUserId, declinerId);
        if (status != PENDING) {
            log.error("[reject] Invalid status: {}", status);
            return Pair.of(false, "Invalid status: " + status);
        }

        UserConnection userConnection = userConnectionRepository.findUserConnectionBy(declinerId, inviterUserId).orElseThrow();
        userConnection.changeStatus(REJECTED);
        cacheService.delete(List.of(
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(declinerId),
                        String.valueOf(inviterUserId)
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(declinerId),
                        userConnection.getStatus().name()
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(inviterUserId),
                        userConnection.getStatus().name()
                )
        ));

        return Pair.of(true, inviterUsername);
    }

    @Transactional
    public Pair<Boolean, String> disconnect(Long callerId, String peerUsername) {
        return userService.findUserIdByUsername(peerUsername)
                .filter(peerUserId -> !callerId.equals(peerUserId))
                .map(peerUserId -> {
                    UserConnectionStatus status = findStatus(callerId, peerUserId);
                    if (status == ACCEPTED) {
                        disconnect(callerId, peerUserId);
                        return Pair.of(true, peerUsername);
                    } else if (status == REJECTED && findInviterUserId(callerId, peerUserId).equals(peerUserId)) {
                        userConnectionRepository.findUserConnectionBy(callerId, peerUserId)
                                .ifPresent(conn -> {
                                    conn.changeStatus(DISCONNECTED);
                                    // 캐시 삭제
                                    cacheService.delete(List.of(
                                            cacheService.generateKey(
                                                    CONNECTION_STATUS,
                                                    String.valueOf(callerId),
                                                    String.valueOf(peerUserId)
                                            ),
                                            cacheService.generateKey(
                                                    CONNECTION_STATUS,
                                                    String.valueOf(callerId),
                                                    conn.getStatus().name()
                                            ),
                                            cacheService.generateKey(
                                                    CONNECTION_STATUS,
                                                    String.valueOf(peerUserId),
                                                    conn.getStatus().name()
                                            )
                                    ));
                                });
                        return Pair.of(true, peerUsername);
                    }
                    return Pair.of(false, "Disconnect failed.");
                })
                .orElseGet(() -> Pair.of(false, "Peer not found."));
    }

    public List<UserReadResponse> findConnectionUsersByStatus(Long userId, UserConnectionStatus status) {
        // 캐시 먼저 조회
        String key = cacheService.generateKey(CONNECTIONS_STATUS, userId.toString(), status.name());
        Optional<String> cachedUsers = cacheService.get(key);
        if (cachedUsers.isPresent()) {
            return deserializeList(cachedUsers.get(), UserReadResponse.class);
        }

        List<ConnectionInformation> partnerASide = userConnectionRepository.findAllUserConnectionByPartnerAUserId(
                userId,
                status.name()
        );
        List<ConnectionInformation> partnerBSide = userConnectionRepository.findAllUserConnectionByPartnerBUserId(
                userId,
                status.name()
        );

        List<UserReadResponse> userReadResponses = getUserReadResponsesByConnectionStatus(
                userId,
                status,
                partnerASide,
                partnerBSide
        );

        // 캐시 저장
        if (!userReadResponses.isEmpty()) {
            serialize(userReadResponses).ifPresent(serializedUserReadResponse ->
                    cacheService.set(key, serializedUserReadResponse, TIME_TO_LIVE)
            );
        }

        return userReadResponses;
    }

    public UserConnectionStatus findStatus(Long inviterUserId, Long partnerUserId) {
        // (1,2) (2,1) 모두 같은 엔티티여야 하고, 데이터베이스에서 이를 처리하지 않았기 때문에, 서버단에서 처리하기 위함
        long lowerUserId = Long.min(inviterUserId, partnerUserId);
        long higherUserId = Long.max(inviterUserId, partnerUserId);

        String key = cacheService.generateKey(
                CONNECTION_STATUS,
                String.valueOf(lowerUserId),
                String.valueOf(higherUserId)
        );

        return cacheService.get(key)
                .map(UserConnectionStatus::valueOf)
                .orElseGet(() -> {
                    UserConnectionStatus status = userConnectionRepository.findUserConnectionBy(lowerUserId, higherUserId)
                            .map(userConnection -> valueOf(userConnection.getStatus().name()))
                            .orElse(NONE);
                    cacheService.set(key, status.name(), TIME_TO_LIVE);

                    return status;
                });
    }

    public long countConnectionByStatus(Long callerUserId, List<Long> partnerUserIds, UserConnectionStatus status) {
        long leftSide = userConnectionRepository.countByPartnerAUserIdAndPartnerBUserIdInAndStatus(
                callerUserId,
                partnerUserIds,
                status.name()
        );
        long rightSide = userConnectionRepository.countByPartnerBUserIdAndPartnerAUserIdInAndStatus(
                callerUserId,
                partnerUserIds,
                status.name()
        );

        return leftSide + rightSide;
    }

    private void accept(Long inviterId, Long acceptorId) {
        // 여기서 min, max는 같은 엔티티임을 보장하는 것이랑 상관 없이 select ... for update로 레코드를 가져왔을 때,
        // 동시에 여러 스레드가 이 메서드를 호출할 수 있는데 그때, 데드락을 피하기 위함. 왜냐하면, A Thread가 1,2를 요청하고 B Thread가 2,1을 동시에 요청하면
        // A는 1에 락걸고 2를 가져오려 할 것이고, B는 2에 락걸고 1을 가져오려 할 것인데 이 둘은 지금 둘 중 하나가 포기하지 않는 이상 락이 절대 풀리지 않게 된다.
        // 그래서 A, B, C, ...Z 까지 모두 동일하게 1 -> 2 순서로 select ... for update로 가져오게 하기 위해 사용
        long partnerAId = Long.min(inviterId, acceptorId);
        long partnerBId = Long.max(inviterId, acceptorId);

        User partnerA = userRepository.findLockByUserId(partnerAId).orElseThrow();
        User partnerB = userRepository.findLockByUserId(partnerBId).orElseThrow();

        UserConnection userConnection = userConnectionRepository.findUserConnectionBy(partnerAId, partnerBId, PENDING.name())
                .orElseThrow();

        if (partnerA.getConnectionCount() >= LIMIT_CONNECTION_COUNT) {
            log.error("[accept] Connection count limit exceeded.");
            throw new IllegalStateException(
                    inviterId.equals(partnerAId) ?
                            "Connection count limit exceeded." :
                            "Connection count limit exceeded by partner."
            );
        }
        if (partnerB.getConnectionCount() >= LIMIT_CONNECTION_COUNT) {
            log.error("[accept] Connection count limit exceeded.");
            throw new IllegalStateException(
                    inviterId.equals(partnerBId) ?
                            "Connection count limit exceeded." :
                            "Connection count limit exceeded by partner."
            );
        }

        partnerA.changeConnectionCount(partnerA.getConnectionCount() + 1);
        partnerB.changeConnectionCount(partnerB.getConnectionCount() + 1);
        userConnection.changeStatus(ACCEPTED);
        cacheService.delete(List.of(
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(partnerA.getUserId()),
                        String.valueOf(partnerB.getUserId())
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(inviterId),
                        userConnection.getStatus().name()
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(acceptorId),
                        userConnection.getStatus().name()
                )
        ));
    }

    private List<UserReadResponse> getUserReadResponsesByConnectionStatus(Long userId,
                                                                          UserConnectionStatus status,
                                                                          List<ConnectionInformation> partnerASide,
                                                                          List<ConnectionInformation> partnerBSide) {
        if (status == ACCEPTED) {
            return Stream.concat(partnerASide.stream(), partnerBSide.stream())
                    .map(UserReadResponse::of)
                    .toList();
        } else {
            return Stream.concat(partnerASide.stream(), partnerBSide.stream())
                    .filter(conn -> !conn.getInviterUserId().equals(userId))
                    .map(UserReadResponse::of)
                    .toList();
        }
    }

    private void disconnect(Long callerId, Long peerId) {
        // 여기서 min, max는 같은 엔티티임을 보장하는 것이랑 상관 없이 select ... for update로 레코드를 가져왔을 때,
        // 동시에 여러 스레드가 이 메서드를 호출할 수 있는데 그때, 데드락을 피하기 위함. 왜냐하면, A Thread가 1,2를 요청하고 B Thread가 2,1을 동시에 요청하면
        // A는 1에 락걸고 2를 가져오려 할 것이고, B는 2에 락걸고 1을 가져오려 할 것인데 이 둘은 지금 둘 중 하나가 포기하지 않는 이상 락이 절대 풀리지 않게 된다.
        // 그래서 A, B, C, ...Z 까지 모두 동일하게 1 -> 2 순서로 select ... for update로 가져오게 하기 위해 사용
        long partnerAId = Long.min(callerId, peerId);
        long partnerBId = Long.max(callerId, peerId);

        User partnerA = userRepository.findLockByUserId(partnerAId).orElseThrow();
        User partnerB = userRepository.findLockByUserId(partnerBId).orElseThrow();

        UserConnection userConnection = userConnectionRepository.findUserConnectionBy(partnerAId, partnerBId, ACCEPTED.name())
                .orElseThrow();

        if (partnerA.getConnectionCount() <= 0) {
            throw new IllegalStateException(partnerA.getUsername() + ": connection count is already 0.");
        }
        if (partnerB.getConnectionCount() <= 0) {
            throw new IllegalStateException(partnerB.getUsername() + ": connection count is already 0.");
        }

        partnerA.changeConnectionCount(partnerA.getConnectionCount() - 1);
        partnerB.changeConnectionCount(partnerB.getConnectionCount() - 1);
        userConnection.changeStatus(DISCONNECTED);
        cacheService.delete(List.of(
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(partnerA.getUserId()),
                        String.valueOf(partnerB.getUserId())
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(callerId),
                        userConnection.getStatus().name()
                ),
                cacheService.generateKey(
                        CONNECTION_STATUS,
                        String.valueOf(peerId),
                        userConnection.getStatus().name()
                )
        ));
    }

    private Long findInviterUserId(Long partnerAUserId, Long partnerBUserId) {
        // (1,2) (2,1) 모두 같은 엔티티여야 하고, 데이터베이스에서 이를 처리하지 않았기 때문에, 서버단에서 처리하기 위함
        long lowerUserId = Long.min(partnerAUserId, partnerBUserId);
        long higherUserId = Long.max(partnerAUserId, partnerBUserId);

        String key = cacheService.generateKey(
                INVITER_USER_ID,
                String.valueOf(lowerUserId),
                String.valueOf(higherUserId)
        );

        return cacheService.get(key)
                .map(Long::valueOf)
                .orElseGet(() -> {
                    Long inviterUserId = userConnectionRepository.findUserConnectionBy(lowerUserId, higherUserId)
                            .map(UserConnection::getInviterUserId)
                            .orElseThrow();
                    cacheService.set(key, String.valueOf(inviterUserId), TIME_TO_LIVE);

                    return inviterUserId;
                });
    }

    private void createConnection(Long inviterUserId, Long partnerUserId) {
        userConnectionRepository.save(
                UserConnection.create(
                        Long.min(inviterUserId, partnerUserId), // (1,2) (2,1) 모두 같은 엔티티여야 하고, 데이터베이스에서 이를 처리하지 않았기 때문에, 서버단에서 처리하기 위함
                        Long.max(inviterUserId, partnerUserId),
                        inviterUserId,
                        PENDING
                )
        );
    }
}
