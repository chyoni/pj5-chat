package cwchoiit.server.auth.service;

import cwchoiit.server.auth.entity.User;
import cwchoiit.server.auth.repository.UserRepository;
import cwchoiit.server.auth.service.request.UserRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cwchoiit.server.auth.constants.KeyPrefix.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SessionService sessionService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final long TIME_TO_LIVE = 3600L;

    @Transactional
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
        cacheService.delete(
                List.of(
                        cacheService.generateKey(USER_ID, findUser.getUsername()),
                        cacheService.generateKey(USERNAME, findUser.getUserId().toString()),
                        cacheService.generateKey(USER, findUser.getUserId().toString()),
                        cacheService.generateKey(USER_INVITECODE, findUser.getUserId().toString())
                )
        );
        userRepository.delete(findUser);
    }
}
