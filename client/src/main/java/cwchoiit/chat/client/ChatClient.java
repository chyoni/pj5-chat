package cwchoiit.chat.client;

import cwchoiit.chat.client.handler.CommandHandler;
import cwchoiit.chat.client.handler.ReceiveMessageHandler;
import cwchoiit.chat.client.handler.WebSocketReceiverHandler;
import cwchoiit.chat.client.handler.WebSocketSenderHandler;
import cwchoiit.chat.client.messages.send.ChatMessageSendMessage;
import cwchoiit.chat.client.service.RestApiService;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.UserService;
import cwchoiit.chat.client.service.WebSocketService;
import org.jline.reader.UserInterruptException;

public class ChatClient {
    public static void main(String[] args) {

        final String BASE_URL = "localhost:80";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService = TerminalService.create();
        UserService userService = new UserService();
        ReceiveMessageHandler receiveMessageHandler = new ReceiveMessageHandler(terminalService, userService);

        RestApiService restApiService = new RestApiService(terminalService, BASE_URL);
        WebSocketSenderHandler webSocketSenderHandler = new WebSocketSenderHandler(terminalService);
        WebSocketService webSocketService = new WebSocketService(
                terminalService,
                userService,
                webSocketSenderHandler,
                BASE_URL,
                WEBSOCKET_ENDPOINT
        );
        webSocketService.setReceiverHandler(new WebSocketReceiverHandler(receiveMessageHandler));
        CommandHandler commandHandler = new CommandHandler(restApiService, webSocketService, terminalService, userService);
        terminalService.printSystemMessage("'/help' Help for commands. ex) /help");

        while (true) {
            try {
                String input = terminalService.readLine("Enter message:").trim();
                if (!input.isEmpty() && (input.charAt(0) == '/')) {
                    String[] parts = input.split(" ", 2);
                    String command = parts[0].substring(1);
                    String argument = parts.length > 1 ? parts[1] : "";

                    if (!commandHandler.process(command, argument)) {
                        break;
                    }

                } else if (userService.isInChannel() && !input.isEmpty()) {
                    // 내가 보낸 메세지 (내 화면에도 찍어야 하니까)
                    terminalService.printMessage(userService.getUsername(), input);

                    // 연결된 상대방에게 보낼 메세지
                    webSocketService.sendMessage(new ChatMessageSendMessage(userService.getChannelId(), input));
                } else {
                    terminalService.printSystemMessage("Invalid message: %s".formatted(input));
                }
            } catch (UserInterruptException e) {
                // 유저가 Ctrl + C 입력으로 강제종료
                terminalService.flush();
                commandHandler.process("exit", "");
                break;
            }
        }
    }
}
