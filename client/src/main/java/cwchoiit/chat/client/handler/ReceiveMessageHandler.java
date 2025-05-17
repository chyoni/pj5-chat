package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.messages.BaseReceiveMessage;
import cwchoiit.chat.client.messages.receive.*;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.common.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReceiveMessageHandler {

    private final TerminalService terminalService;

    public void handle(String payload) {
        Serializer.deserialize(payload, BaseReceiveMessage.class)
                .ifPresent(this::handleMessage);
    }

    private void handleMessage(BaseReceiveMessage message) {
        if (message instanceof ChatMessageReceiveMessage receivedMessage) {
            terminalService.printMessage(
                    receivedMessage.getUsername(),
                    receivedMessage.getContent()
            );
        }
        if (message instanceof FetchUserInviteCodeReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Your invite code is: %s".formatted(receivedMessage.getInviteCode()));
        }
        if (message instanceof InviteReceiveMessage receivedMessage) {
            terminalService.printSystemMessage(
                    "Invite %s, result: %s".formatted(receivedMessage.getConnectionInviteCode(), receivedMessage.getStatus())
            );
        }
        if (message instanceof InviteNotificationReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Do you accept the invite from %s?".formatted(receivedMessage.getUsername()));
        }
        if (message instanceof AcceptReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Connection accepted by %s.".formatted(receivedMessage.getUsername()));
        }
        if (message instanceof AcceptNotificationReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Connection accepted by %s.".formatted(receivedMessage.getUsername()));
        }
        if (message instanceof RejectReceiveMessage receivedMessage) {
            terminalService.printSystemMessage(
                    "Connection rejected with %s. status: %s".formatted(receivedMessage.getUsername(), receivedMessage.getStatus())
            );
        }
        if (message instanceof DisconnectReceiveMessage receivedMessage) {
            terminalService.printSystemMessage(
                    "Connection disconnected with %s. status: %s".formatted(receivedMessage.getUsername(), receivedMessage.getStatus())
            );
        }
        if (message instanceof FetchConnectionsReceiveMessage receivedMessage) {
            if (receivedMessage.getConnections().isEmpty()) {
                terminalService.printSystemMessage("No connections found.");
            } else {
                receivedMessage.getConnections().forEach(connection ->
                        terminalService.printSystemMessage("%s : %s".formatted(connection.username(), connection.status()))
                );
            }
        }
        if (message instanceof ErrorReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Error: %s".formatted(receivedMessage.getMessage()));
        }
    }
}
