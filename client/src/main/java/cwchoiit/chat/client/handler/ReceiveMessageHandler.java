package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.messages.BaseReceiveMessage;
import cwchoiit.chat.client.messages.receive.*;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.UserService;
import cwchoiit.chat.common.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReceiveMessageHandler {

    private final TerminalService terminalService;
    private final UserService userService;

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
            terminalService.printSystemMessage("Invite %s, result: %s".formatted(receivedMessage.getConnectionInviteCode(), receivedMessage.getStatus()));
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
            terminalService.printSystemMessage("Connection rejected with %s. status: %s".formatted(receivedMessage.getUsername(), receivedMessage.getStatus()));
        }
        if (message instanceof DisconnectReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Connection disconnected with %s. status: %s".formatted(receivedMessage.getUsername(), receivedMessage.getStatus()));
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
        if (message instanceof CreateChannelReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Channel created. Channel ID: %s".formatted(receivedMessage.getChannelId()));
        }
        if (message instanceof ChannelJoinNotificationReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Join channel [%s], channel ID: %s".formatted(receivedMessage.getTitle(), receivedMessage.getChannelId()));
        }
        if (message instanceof EnterChannelReceiveMessage receivedMessage) {
            userService.moveToChannel(receivedMessage.getChannelId());
            terminalService.printSystemMessage("Enter channel [%s], channel ID: %s".formatted(receivedMessage.getTitle(), receivedMessage.getChannelId()));
        }
        if (message instanceof ErrorReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Error: %s".formatted(receivedMessage.getMessage()));
        }
        if (message instanceof FetchChannelsReceiveMessage receivedMessage) {
            receivedMessage.getChannels()
                    .forEach(channel ->
                            terminalService.printSystemMessage("%s : %s (%d)".formatted(channel.channelId(), channel.title(), channel.headCount()))
                    );
        }
        if (message instanceof FetchChannelInviteCodeReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Channel ID: %d, invite code is: %s".formatted(receivedMessage.getChannelId(), receivedMessage.getInviteCode()));
        }
        if (message instanceof JoinChannelReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Joined channel ID: %d - Title: %s".formatted(receivedMessage.getChannelId(), receivedMessage.getTitle()));
        }
        if (message instanceof LeaveChannelReceiveMessage ignored) {
            terminalService.printSystemMessage("Leave channel ID: %s".formatted(userService.getChannelId()));
            userService.moveToLobby();
        }
        if (message instanceof QuitChannelReceiveMessage receivedMessage) {
            terminalService.printSystemMessage("Quit channel ID: %s".formatted(receivedMessage.getChannelId()));
        }
    }
}
