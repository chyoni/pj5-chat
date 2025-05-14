package cwchoiit.chat.server.service;

import cwchoiit.chat.server.constants.UserConnectionStatus;
import cwchoiit.chat.server.entity.UserConnection;
import cwchoiit.chat.server.repository.UserConnectionRepository;
import cwchoiit.chat.server.service.response.UserReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserConnectionService {

    private final UserService userService;
    private final UserConnectionRepository userConnectionRepository;

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
                String inviterUsername = userService.findUsernameByUserId(inviterUserId).orElse(null);
                if (inviterUsername == null) {
                    log.error("[invite] inviter not found: {}", inviterUserId);
                    yield Pair.of(Optional.empty(), "inviter not found: " + inviterUserId);
                }
                updateStatus(inviterUserId, partner.userId(), UserConnectionStatus.PENDING);
                yield Pair.of(Optional.of(partner.userId()), inviterUsername);
            }
            case ACCEPTED -> Pair.of(Optional.empty(), "Already invited.");
            case PENDING, REJECTED -> Pair.of(Optional.empty(), "Already invited or rejected.");
        };
    }

    private UserConnectionStatus findStatus(Long inviterUserId, Long partnerUserId) {
        return userConnectionRepository.findUserConnectionBy(
                        Long.min(inviterUserId, partnerUserId),
                        Long.max(inviterUserId, partnerUserId)
                ).map(userConnection -> UserConnectionStatus.valueOf(userConnection.getStatus().name()))
                .orElse(UserConnectionStatus.NONE);
    }

    private void updateStatus(Long inviterUserId, Long partnerUserId, UserConnectionStatus status) {
        if (status == UserConnectionStatus.ACCEPTED) {
            throw new IllegalArgumentException("Cannot update status to ACCEPTED.");
        }

        userConnectionRepository.save(
                UserConnection.create(
                        Long.min(inviterUserId, partnerUserId),
                        Long.max(inviterUserId, partnerUserId),
                        inviterUserId,
                        status
                )
        );
    }
}
