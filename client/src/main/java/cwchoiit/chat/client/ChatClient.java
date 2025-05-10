package cwchoiit.chat.client;

import cwchoiit.chat.client.service.TerminalService;

public class ChatClient {
    public static void main(String[] args) {
        TerminalService terminalService = TerminalService.create();

        while (true) {
            String input = terminalService.readLine("Enter message:").trim();
            if (!input.isEmpty() && (input.charAt(0) == '/')) {
                String command = input.substring(1);
                if (command.equals("exit")) {
                    break;
                } else if (command.equals("clear")) {
                    terminalService.clearTerminal();
                }
            } else if (!input.isEmpty()) {
                terminalService.printMessage("test", input);
            }
        }
    }
}
