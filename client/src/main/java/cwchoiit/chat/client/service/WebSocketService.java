package cwchoiit.chat.client.service;

import cwchoiit.chat.client.dto.Message;
import cwchoiit.chat.client.handler.WebSocketReceiverHandler;
import cwchoiit.chat.client.handler.WebSocketSenderHandler;
import cwchoiit.chat.client.handler.WebSocketSessionHandler;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Setter;
import org.glassfish.tyrus.client.ClientManager;

import java.net.URI;

/**
 * Provides functionality to establish, manage, and close a WebSocket connection.
 * This service supports interacting with WebSocket servers by enabling sending
 * and receiving messages in real-time.
 * <p>
 * Responsibilities:
 * - Establishing a WebSocket connection.
 * - Sending messages through the open WebSocket connection.
 * - Receiving messages via a configurable {@link WebSocketReceiverHandler}.
 * - Graceful session closure.
 * <p>
 * Components:
 * - {@link TerminalService}: Used to log system and user-related messages.
 * - {@link WebSocketSenderHandler}: Handles the logic for sending messages through the WebSocket.
 * - {@link WebSocketReceiverHandler}: Processes incoming messages.
 * <p>
 * Behavior:
 * - The service manages the WebSocket lifecycle, including connection establishment,
 * communication, and session closure.
 * - Deploys handlers for message sending and receiving to facilitate communication.
 * - Notifies the terminal of significant WebSocket events, such as errors or closure.
 */
public class WebSocketService {
    private final TerminalService terminalService;
    private final WebSocketSenderHandler senderHandler;
    private final String webSocketUrl;

    @Setter
    private WebSocketReceiverHandler receiverHandler;
    private Session session;

    public WebSocketService(TerminalService terminalService, WebSocketSenderHandler senderHandler, String url, String endpoint) {
        this.terminalService = terminalService;
        this.senderHandler = senderHandler;
        this.webSocketUrl = "ws://" + url + endpoint;
    }

    public void createSession() {
        ClientManager clientManager = ClientManager.createClient();
        try {
            session = clientManager.connectToServer(new WebSocketSessionHandler(terminalService), new URI(webSocketUrl));
            session.addMessageHandler(receiverHandler);
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to connect to server: " + webSocketUrl + ". error: " + e.getMessage());
        }
    }

    public void closeSession() {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure"));
                }
                session = null;
            }
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to close to server: " + webSocketUrl + ". error: " + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        if (session != null && session.isOpen()) {
            senderHandler.sendMessage(session, message);
        } else {
            terminalService.printSystemMessage("Failed to send message: " + message.content() + ". session is not open.");
        }
    }
}
