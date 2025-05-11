package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.dto.Message;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.serializer.Serializer;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * Handles sending WebSocket messages.
 * <p>
 * This class is responsible for facilitating the sending of messages through an active WebSocket session. It validates the session's state, processes the provided message,
 * and transmits the content to the remote endpoint.
 * <p>
 * Responsibilities:
 * - Ensures that the WebSocket session is active and ready to transmit messages.
 * - Deserializes the message content into a format suitable for transmission.
 * - Sends the processed message content through the WebSocket session.
 * - Handles errors occurring during the message transmission and notifies via the terminal.
 * <p>
 * Dependencies:
 * - {@link TerminalService}: Used to log system messages related to transmission failures or other notable events.
 * - {@link Serializer}: Utilized for deserializing the message content to ensure proper formatting.
 */
@RequiredArgsConstructor
public class WebSocketSenderHandler {

    private final TerminalService terminalService;

    public void sendMessage(Session session, Message message) {
        if (session != null && session.isOpen()) {
            Serializer.serialize(message)
                    .ifPresent(serializedMessage -> processSendMessage(session, serializedMessage));
        }
    }

    private void processSendMessage(Session session, String serializedMessage) {
        try {
            session.getBasicRemote().sendText(serializedMessage);
        } catch (IOException e) {
            terminalService.printSystemMessage("Failed to send message: %s. error: %s".formatted(serializedMessage, e.getMessage()));
        }
    }
}
