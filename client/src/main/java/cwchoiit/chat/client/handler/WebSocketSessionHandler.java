package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.WebSocketService;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;

/**
 * Handles WebSocket session lifecycle events.
 * <p>
 * This class extends the {@link Endpoint} base class to handle events related
 * to a WebSocket session, including session opening, session closing, and error
 * occurrences. It leverages the {@link TerminalService} to display system messages
 * related to these events.
 * <p>
 * Responsibilities:
 * - Displaying a system message when the WebSocket session is opened.
 * - Displaying a system message when the WebSocket session is closed, including the
 * reason for closure.
 * - Displaying a system message when an error occurs during the WebSocket session.
 * <p>
 * Dependencies:
 * - {@link TerminalService}: Used to print formatted system messages to the terminal.
 */
@RequiredArgsConstructor
public class WebSocketSessionHandler extends Endpoint {

    private final TerminalService terminalService;
    private final WebSocketService webSocketService;

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        terminalService.printSystemMessage("WebSocket connected. ");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        webSocketService.closeSession();
        terminalService.printSystemMessage("WebSocket session close. Reason: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        terminalService.printSystemMessage("WebSocket error: " + thr.getMessage());
    }
}
