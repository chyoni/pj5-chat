package cwchoiit.chat.client.service;

import cwchoiit.chat.client.handler.WebSocketReceiverHandler;
import cwchoiit.chat.client.handler.WebSocketSenderHandler;
import cwchoiit.chat.client.handler.WebSocketSessionHandler;
import cwchoiit.chat.client.messages.BaseSendMessage;
import cwchoiit.chat.client.messages.send.KeepAliveSendMessage;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Setter;
import org.glassfish.tyrus.client.ClientManager;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final UserService userService;
    private final WebSocketSenderHandler senderHandler;
    private final String webSocketUrl;

    @Setter
    private WebSocketReceiverHandler receiverHandler;
    /**
     * Represents the active WebSocket session.
     * <p>
     * This variable holds a reference to the current WebSocket session, enabling
     * interaction with the server through WebSocket messages. It is involved in
     * sending messages, receiving responses, and managing the session lifecycle.
     * <p>
     * Responsibilities:
     * - Used to establish, maintain, and terminate the WebSocket connection.
     * - Acts as a communication channel for sending and receiving messages.
     * - Managed by methods such as `createSession`, `closeSession`, and `sendMessage`.
     * <p>
     * Lifecycle:
     * - Initialized when a WebSocket connection is successfully established.
     * - Set to null when the session is closed or invalidated.
     * - Automatically configured with message handlers during the setup process.
     * <p>
     * Accessibility:
     * - Private to ensure that session management is controlled internally within
     * the class.
     * - Interactions with the session are provided via public methods of the
     * containing class.
     */
    private Session session;

    /**
     * Scheduler for sending keep-alive messages to the server every minute.
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public WebSocketService(TerminalService terminalService,
                            UserService userService,
                            WebSocketSenderHandler senderHandler,
                            String url,
                            String endpoint) {
        this.terminalService = terminalService;
        this.userService = userService;
        this.senderHandler = senderHandler;
        this.webSocketUrl = "ws://" + url + endpoint;
    }

    /**
     * Establishes a WebSocket session with the server using the provided session ID.
     * <p>
     * This method attempts to establish a WebSocket session, using the provided session ID
     * to authenticate the connection via a "Cookie" header. If successful, the session is configured
     * with message handlers and keep-alive functionality is enabled. Any errors encountered
     * during this process will result in a failure message being printed to the terminal.
     *
     * @param sessionId the session ID used for authenticating the WebSocket connection
     * @return true if the session was successfully established, false otherwise
     */
    public boolean createSession(String sessionId) {
        ClientManager clientManager = ClientManager.createClient();

        // WebSocket 연결하기 위해 로그인 시 받은 세션 ID를 HTTP Handshake 할 때 넣어준다
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Cookie", List.of("SESSION=" + sessionId));
            }
        };
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(configurator).build();

        try {
            // WebSocket 연결 후 연결된 WebSocket 세션을 저장
            session = clientManager.connectToServer(
                    new WebSocketSessionHandler(terminalService, this, userService),
                    clientEndpointConfig,
                    new URI(webSocketUrl)
            );
            // WebSocket 세션에 메시지 핸들러 적용
            session.addMessageHandler(receiverHandler);
            enableKeepAlive();
            return true;
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to connect to server: " + webSocketUrl + ". error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the current WebSocket session and performs necessary cleanup actions.
     * <p>
     * This method disables the keep-alive functionality and ensures that the WebSocket
     * session is properly closed if it is open. If any errors occur while attempting
     * to close the session, a system message is printed with the error details.
     * <p>
     * The session is set to null after closure to indicate that no active WebSocket
     * connection exists. If the session is already null or not open, no additional
     * actions are performed.
     */
    public void closeSession() {
        try {
            disableKeepAlive();
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

    /**
     * Sends a message to the server through the WebSocket session.
     * If the WebSocket session is not open, an error message is printed to the terminal.
     *
     * @param request the request containing the message data to be sent
     */
    public void sendMessage(BaseSendMessage request) {
        if (session != null && session.isOpen()) {
            senderHandler.sendMessage(session, request);
        } else {
            terminalService.printSystemMessage("Failed to send message. message type is : " + request.getType() + ". reason: session is not open.");
        }
    }

    /**
     * Sends a keep-alive message to the server every minute.
     */
    private void enableKeepAlive() {
        scheduledExecutorService.scheduleAtFixedRate(() ->
                        sendMessage(new KeepAliveSendMessage()),
                1,
                1,
                TimeUnit.MINUTES
        );
    }

    /**
     * Stops the keep-alive message scheduler and shuts down the executor service.
     */
    private void disableKeepAlive() {
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
