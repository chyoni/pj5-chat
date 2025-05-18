package cwchoiit.chat.server.auth;

import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

import static cwchoiit.chat.server.constants.IdKey.HTTP_SESSION_ID;
import static cwchoiit.chat.server.constants.IdKey.USER_ID;

/**
 * This class is an implementation of {@link HttpSessionHandshakeInterceptor} responsible for intercepting
 * WebSocket handshake requests. It ensures that an HTTP session exists before establishing a WebSocket
 * connection and associates the session ID with the WebSocket session attributes.
 * <p>
 * By validating session existence before completing the handshake, this interceptor helps ensure
 * that only authenticated users or requests with valid sessions can initiate WebSocket connections.
 * If the session is null or the request is invalid, the handshake process is terminated with an
 * appropriate HTTP status code.
 * <p>
 * This class uses Spring's WebSocket and HTTP abstraction libraries for request validation and
 * response handling, and it is designed to work with a servlet-based web framework.
 */
@Slf4j
@Component
public class WebSocketHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    /**
     * Intercepts and processes an incoming WebSocket handshake request before the handshake is established.
     * This method validates the existence of an HTTP session in the request and stores the session ID
     * into the WebSocket attributes if the session exists. If the session does not exist, or if the
     * request is invalid, the handshake process is terminated with an appropriate HTTP status code.
     *
     * @param request    the server-side HTTP request to validate, typically containing details about the handshake request
     * @param response   the server-side HTTP response, typically used to modify or terminate the response if validation fails
     * @param wsHandler  the handler for processing WebSocket messages, intercepted during the handshake phase
     * @param attributes a map of WebSocket session attributes to be populated or accessed during the handshake process
     * @return {@code true} if the handshake process is allowed to proceed, {@code false} otherwise
     * @throws Exception if an error occurs during the handshake validation process
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpSession httpSession = servletServerHttpRequest.getServletRequest().getSession(false);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) { // 인증 실패
                log.error("[beforeHandshake] WebSocket handshake failed. Authentication is null. request: {}", servletServerHttpRequest.getServletRequest().getRequestURI());
                return false;
            }
            if (httpSession == null) { // 세션이 없는 경우
                log.warn("[beforeHandshake] WebSocket handshake failed. HttpSession is null. request: {}", servletServerHttpRequest.getServletRequest().getRequestURI());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            attributes.put(HTTP_SESSION_ID.getValue(), httpSession.getId());
            attributes.put(USER_ID.getValue(), userDetails.getUserId());
            return true;
        } else { // Tomcat이 처리할 수 없는 요청인 경우
            log.warn("[beforeHandshake] WebSocket handshake failed. request is not ServletServerHttpRequest. request: {}", request.getURI());
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }
}
