package cwchoiit.chat.client.service;

import cwchoiit.chat.client.dto.UserRegisterRequest;
import cwchoiit.chat.serializer.Serializer;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Getter
public class RestApiService {

    private final TerminalService terminalService;
    private final String url;
    private String sessionId;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public RestApiService(TerminalService terminalService, String url) {
        this.terminalService = terminalService;
        this.url = "http://%s".formatted(url);
    }

    public boolean register(String username, String password) {
        return call("/api/v1/user/register", "", new UserRegisterRequest(username, password))
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    public boolean unregister() {
        if (sessionId.isEmpty()) {
            return false;
        }

        return call("/api/v1/user/unregister", sessionId, null)
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    public boolean login(String username, String password) {
        return call("/api/v1/auth/login", "", new UserRegisterRequest(username, password))
                .filter(response -> response.statusCode() == 200)
                .map(response -> {
                    sessionId = response.body();
                    return true;
                })
                .orElse(false);
    }

    public boolean logout() {
        if (sessionId.isEmpty()) {
            return false;
        }

        return call("/api/v1/auth/logout", sessionId, null)
                .map(response -> response.statusCode() == 200)
                .orElse(false);
    }

    private Optional<HttpResponse<String>> call(String path, String sessionId, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url + path))
                    .header("Content-Type", "application/json");

            if (!sessionId.isEmpty()) {
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
