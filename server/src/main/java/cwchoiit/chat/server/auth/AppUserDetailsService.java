package cwchoiit.chat.server.auth;

import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.info("[loadUserByUsername] user not found: {}", username);
                    return new UsernameNotFoundException("user not found: " + username);
                });

        return new AppUserDetails(user.getUserId(), user.getUsername(), user.getPassword());
    }
}
