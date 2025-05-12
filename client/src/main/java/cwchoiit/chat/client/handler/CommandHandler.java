package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.service.RestApiService;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.WebSocketService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class CommandHandler {
    private final RestApiService restApiService;
    private final WebSocketService webSocketService;
    private final TerminalService terminalService;
    private final Map<String, Function<String[], Boolean>> commands = Map.of(
            "register", this::register,
            "unregister", this::unregister,
            "login", this::login,
            "logout", this::logout,
            "clear", this::clear,
            "exit", this::exit
    );


    public boolean process(String command, String argument) {
        Function<String[], Boolean> commander = commands.getOrDefault(command, (ignored) -> {
            terminalService.printSystemMessage("Invalid command: %s".formatted(command));
            return true;
        });

        return commander.apply(argument.split(" "));
    }

    private Boolean register(String[] params) {
        if (params.length > 1) {
            if (restApiService.register(params[0], params[1])) {
                terminalService.printSystemMessage("Registered.");
            } else {
                terminalService.printSystemMessage("Failed to register.");
            }
        }
        return true;
    }

    private Boolean unregister(String[] params) {
        webSocketService.closeSession();
        if (restApiService.unregister()) {
            terminalService.printSystemMessage("Unregistered.");
        } else {
            terminalService.printSystemMessage("Failed to unregister.");
        }
        return true;
    }

    private Boolean login(String[] params) {
        if (params.length > 1) {
            if (restApiService.login(params[0], params[1])) {
                if (webSocketService.createSession(restApiService.getSessionId())) {
                    terminalService.printSystemMessage("Logged in.");
                }
            } else {
                terminalService.printSystemMessage("Failed to login.");
            }
        }
        return true;
    }

    private Boolean logout(String[] params) {
        webSocketService.closeSession();
        if (restApiService.logout()) {
            terminalService.printSystemMessage("Logged out.");
        } else {
            terminalService.printSystemMessage("Failed to logout.");
        }
        return true;
    }

    private Boolean clear(String[] params) {
        terminalService.clearTerminal();
        return true;
    }

    private Boolean exit(String[] params) {
        webSocketService.closeSession();
        terminalService.printSystemMessage("Exiting...");
        return false;
    }
}
