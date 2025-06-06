package cwchoiit.chat.client.service;

import cwchoiit.chat.client.service.request.UserRegisterRequest;
import cwchoiit.chat.common.serializer.Serializer;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Service for handling REST API interactions.
 * This class provides functionalities for user authentication and operations
 * such as registration, login, logout, and unregistration by communicating
 * with a REST API through HTTP requests.
 * <p>
 * The service relies on the TerminalService for logging and displaying messages
 * to the user. HTTP requests are constructed using the {@link HttpClient} and
 * serialized using a utility class for processing request bodies.
 */
@Getter
public class RestApiService {

    private final TerminalService terminalService;
    private final String url;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String sessionId;

    public RestApiService(TerminalService terminalService, String url) {
        this.terminalService = terminalService;
        this.url = "http://%s".formatted(url);
    }

    /**
     * Registers a new user account with the REST API.
     *
     * @param username The username to be registered. Must be unique.
     * @param password The password to be registered.
     * @return true if the registration was successful, false otherwise.
     */
    public boolean register(String username, String password) {
        return call("/api/v1/user/register", "", new UserRegisterRequest(username, password))
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    /**
     * Unregisters the current user account from the REST API.
     *
     * @return true if the unregistration was successful, false otherwise.
     */
    public boolean unregister() {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        return call("/api/v1/user/unregister", sessionId, null)
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    /**
     * Attempts to log in the user with the provided credentials.
     *
     * @param username The username to be authenticated. Must match the username used during registration.
     * @param password The password to be authenticated. Must match the password used during registration.
     * @return true if the login was successful, false otherwise.
     */
    public boolean login(String username, String password) {
        return call("/api/v1/auth/login", "", new UserRegisterRequest(username, password))
                .filter(response -> response.statusCode() == 200)
                .map(response -> {
                    sessionId = response.body();
                    return true;
                })
                .orElse(false);
    }

    /**
     * Attempts to log out the user from the REST API.
     *
     * @return true if the logout was successful, false otherwise.
     */
    public boolean logout() {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }

        return call("/api/v1/auth/logout", sessionId, null)
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    /**
     * Sends an HTTP request to the REST API with the provided path, session ID, and body.
     *
     * @param path      The path to the API endpoint. Must start with a forward slash.
     * @param sessionId The session ID to be included in the request headers. Can be empty.
     * @param body      The request body to be sent. Can be null.
     * @return An Optional containing the response if the request was successful, or an empty Optional otherwise.
     */
    private Optional<HttpResponse<String>> call(String path, @Nullable String sessionId, @Nullable Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url + path))
                    .header("Content-Type", "application/json");

            if (sessionId != null && !sessionId.isEmpty()) {
                builder.header("Cookie", "SESSION=" + sessionId);
            }

            if (body != null) {
                Serializer.serialize(body).ifPresent(payload ->
                        builder.POST(HttpRequest.BodyPublishers.ofString(payload))
                );
            } else {
                builder.POST(HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            terminalService.printSystemMessage("Response status: %d, body: %s".formatted(response.statusCode(), response.body()));
            return Optional.of(response);
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to call API: " + e.getMessage());
            return Optional.empty();
        }
    }
}
