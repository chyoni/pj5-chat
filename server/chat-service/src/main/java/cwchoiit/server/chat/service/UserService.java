package cwchoiit.server.chat.service;

import cwchoiit.chat.common.serializer.Serializer;
import cwchoiit.server.chat.entity.User;
import cwchoiit.server.chat.repository.UserRepository;
import cwchoiit.server.chat.service.response.UserReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static cwchoiit.server.chat.constants.KeyPrefix.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final CacheService cacheService;
    private final UserRepository userRepository;

    private final long TIME_TO_LIVE = 3600L;

    public Optional<String> findUsernameByUserId(Long userId) {
        String cacheUsernameKey = cacheService.generateKey(USERNAME, userId.toString());

        return cacheService.get(cacheUsernameKey)
                .or(() -> {
                    Optional<String> username = userRepository.findByUserId(userId).map(User::getUsername);
                    username.ifPresent(findUsername -> cacheService.set(
                                    cacheUsernameKey,
                                    findUsername,
                                    TIME_TO_LIVE
                            )
                    );
                    return username;
                });
    }

    public List<Long> findUserIdsByUsernames(List<String> usernames) {
        return userRepository.findAllByUsernameIn(usernames).stream()
                .map(User::getUserId)
                .toList();
    }

    public Optional<Long> findUserIdByUsername(String username) {
        String cacheUserIdKey = cacheService.generateKey(USER_ID, username);

        return cacheService.get(cacheUserIdKey)
                .map(Long::parseLong)
                .or(() -> {
                    Optional<Long> userId = userRepository.findByUsername(username).map(User::getUserId);
                    userId.ifPresent(id -> cacheService.set(cacheUserIdKey, id.toString(), TIME_TO_LIVE));
                    return userId;
                });
    }

    public Optional<String> findInviteCodeByUserId(Long userId) {
        String cacheUserInviteCodeKey = cacheService.generateKey(USER_INVITECODE, userId.toString());

        return cacheService.get(cacheUserInviteCodeKey)
                .or(() -> {
                    Optional<String> inviteCode = userRepository
                            .findByUserId(userId)
                            .map(User::getConnectionInviteCode);

                    inviteCode.ifPresent(code -> cacheService.set(cacheUserInviteCodeKey, code, TIME_TO_LIVE));

                    return inviteCode;
                });
    }

    public Optional<Integer> findConnectionCountByUserId(Long userId) {
        return userRepository.findByUserId(userId).map(User::getConnectionCount);
    }

    public Optional<UserReadResponse> findUserByConnectionInviteCode(String connectionInviteCode) {
        String cacheUserKey = cacheService.generateKey(USER, connectionInviteCode);

        return cacheService.get(cacheUserKey)
                .flatMap(user -> Serializer.deserialize(user, UserReadResponse.class))
                .or(() -> {
                    Optional<UserReadResponse> userReadResponse = userRepository
                            .findByConnectionInviteCode(connectionInviteCode)
                            .map(UserReadResponse::of);

                    userReadResponse.flatMap(Serializer::serialize)
                            .ifPresent(response -> cacheService.set(cacheUserKey, response, TIME_TO_LIVE));

                    return userReadResponse;
                });
    }
}
