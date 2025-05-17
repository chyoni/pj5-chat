package cwchoiit.chat.client;

import cwchoiit.chat.client.handler.CommandHandler;
import cwchoiit.chat.client.handler.ReceiveMessageHandler;
import cwchoiit.chat.client.handler.WebSocketReceiverHandler;
import cwchoiit.chat.client.handler.WebSocketSenderHandler;
import cwchoiit.chat.client.messages.send.ChatMessageSendMessage;
import cwchoiit.chat.client.service.RestApiService;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.WebSocketService;

public class ChatClient {
    public static void main(String[] args) {

        final String BASE_URL = "localhost:8080";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService = TerminalService.create();
        ReceiveMessageHandler receiveMessageHandler = new ReceiveMessageHandler(terminalService);

        RestApiService restApiService = new RestApiService(terminalService, BASE_URL);
        WebSocketSenderHandler webSocketSenderHandler = new WebSocketSenderHandler(terminalService);
        WebSocketService webSocketService = new WebSocketService(
                terminalService,
                webSocketSenderHandler,
                BASE_URL,
                WEBSOCKET_ENDPOINT
        );
        webSocketService.setReceiverHandler(new WebSocketReceiverHandler(receiveMessageHandler));
        CommandHandler commandHandler = new CommandHandler(restApiService, webSocketService, terminalService);

        while (true) {
            String input = terminalService.readLine("Enter message:").trim();
            if (!input.isEmpty() && (input.charAt(0) == '/')) {
                String[] parts = input.split(" ", 2);
                String command = parts[0].substring(1);
                String argument = parts.length > 1 ? parts[1] : "";

                if (!commandHandler.process(command, argument)) {
                    break;
                }

            } else if (!input.isEmpty()) {
                terminalService.printMessage("[Me]", input);
                webSocketService.sendMessage(new ChatMessageSendMessage("[Client]", input));
            }
        }
    }
}
