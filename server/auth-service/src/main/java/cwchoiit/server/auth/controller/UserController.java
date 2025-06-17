package cwchoiit.server.auth.controller;

import cwchoiit.server.auth.service.UserService;
import cwchoiit.server.auth.service.request.UserRegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok("Registered.");
    }

    @PostMapping("/unregister")
    public ResponseEntity<String> unregister(HttpServletRequest request) {
        userService.removeUser();
        request.getSession().invalidate();
        return ResponseEntity.ok("Unregistered.");
    }
}
