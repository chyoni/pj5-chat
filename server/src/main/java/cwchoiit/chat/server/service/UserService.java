package cwchoiit.chat.server.service;

import cwchoiit.chat.server.service.request.UserRegisterRequest;
import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        userRepository.delete(findUser);
    }
}
