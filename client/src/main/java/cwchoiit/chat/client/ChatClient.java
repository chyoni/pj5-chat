package cwchoiit.chat.client;

import cwchoiit.chat.client.dto.Message;
import cwchoiit.chat.client.handler.WebSocketReceiverHandler;
import cwchoiit.chat.client.handler.WebSocketSenderHandler;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.WebSocketService;

public class ChatClient {
    public static void main(String[] args) {

        final String WEBSOCKET_BASE_URL = "localhost:8080";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService = TerminalService.create();

        WebSocketSenderHandler webSocketSenderHandler = new WebSocketSenderHandler(terminalService);
        WebSocketService webSocketService = new WebSocketService(terminalService, webSocketSenderHandler, WEBSOCKET_BASE_URL, WEBSOCKET_ENDPOINT);
        webSocketService.setReceiverHandler(new WebSocketReceiverHandler(terminalService));

        while (true) {
            String input = terminalService.readLine("Enter message:").trim();
            if (!input.isEmpty() && (input.charAt(0) == '/')) {
                String command = input.substring(1);

                boolean exit = switch (command) {
                    case "exit" -> {
                        webSocketService.closeSession();
                        yield true;
                    }
                    case "clear" -> {
                        terminalService.clearTerminal();
                        yield false;
                    }
                    case "connect" -> {
                        webSocketService.createSession();
                        yield false;
                    }
                    default -> false;
                };

                if (exit) {
                    break;
                }
            } else if (!input.isEmpty()) {
                terminalService.printMessage("[Me]", input);
                webSocketService.sendMessage(new Message("[Client]", input));
            }
        }
    }
}
