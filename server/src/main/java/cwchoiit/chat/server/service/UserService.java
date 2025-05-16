package cwchoiit.chat.server.service;

import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.repository.UserRepository;
import cwchoiit.chat.server.service.request.UserRegisterRequest;
import cwchoiit.chat.server.service.response.UserReadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @SuppressWarnings("all")
    public Long createUser(UserRegisterRequest request) {
        User newUser = userRepository.save(
                User.create(
                        request.username(),
                        passwordEncoder.encode(request.password())
                )
        );
        return newUser.getUserId();
    }

    @Transactional
    public void removeUser() {
        User findUser = userRepository.findByUsername(sessionService.findUsername()).orElseThrow();
        userRepository.delete(findUser);
    }

    public Optional<String> findUsernameByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .map(User::getUsername);
    }

    public Optional<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getUserId);
    }

    public Optional<String> findInviteCodeByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .map(User::getConnectionInviteCode);
    }

    public Optional<Integer> findConnectionCountByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .map(User::getConnectionCount);
    }

    public Optional<UserReadResponse> findUserByConnectionInviteCode(String connectionInviteCode) {
        return userRepository.findByConnectionInviteCode(connectionInviteCode)
                .map(UserReadResponse::of);
    }
}
