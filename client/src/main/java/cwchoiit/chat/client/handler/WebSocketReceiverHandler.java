package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.service.TerminalService;
import jakarta.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;

/**
 * Handles incoming WebSocket text messages.
 * This class implements the MessageHandler.Whole interface for processing
 * complete textual messages received from a WebSocket connection.
 * <p>
 * The {@link TerminalService} is used to facilitate interaction with the terminal,
 * such as printing messages or performing terminal-specific operations.
 * <p>
 * Responsibilities:
 * - Processes textual messages received from a WebSocket client or server.
 * - Leveraging {@link TerminalService} to display the received messages or
 * perform other terminal interactions.
 * <p>
 * Dependencies:
 * - {@link TerminalService}: A service that provides terminal-related functionalities,
 * such as printing formatted messages to the terminal or clearing the terminal screen.
 */
@RequiredArgsConstructor
public class WebSocketReceiverHandler implements MessageHandler.Whole<String> {

    private final ReceiveMessageHandler receiveMessageHandler;

    @Override
    public void onMessage(String payload) {
        receiveMessageHandler.handle(payload);
    }
}
