package cwchoiit.chat.server.config;

import cwchoiit.chat.server.auth.RestApiLoginAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   AuthenticationManager authenticationManager) throws Exception {
        RestApiLoginAuthFilter restApiLoginAuthFilter = new RestApiLoginAuthFilter(
                new AntPathRequestMatcher("/api/v1/auth/login", "POST"),
                authenticationManager
        );

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterAt(restApiLoginAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/v1/auth/login").permitAll()
                                .anyRequest().authenticated()
                )
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout").logoutSuccessHandler(this::logoutHandler)
                );

        return httpSecurity.build();
    }

    private void logoutHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message;
        if (authentication != null && authentication.isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_OK);
            message = "logout success";
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            message = "logout failed";
        }

        try {
            response.getWriter().write(message);
        } catch (IOException e) {
            log.error("[logoutHandler] failed to send response. ", e);
        }
    }
}
