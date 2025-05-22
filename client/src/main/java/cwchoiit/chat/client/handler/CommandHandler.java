package cwchoiit.chat.client.handler;

import cwchoiit.chat.client.constants.UserConnectionStatus;
import cwchoiit.chat.client.messages.send.*;
import cwchoiit.chat.client.service.RestApiService;
import cwchoiit.chat.client.service.TerminalService;
import cwchoiit.chat.client.service.UserService;
import cwchoiit.chat.client.service.WebSocketService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles user commands and dispatches them to the appropriate services for processing.
 * This class provides a mechanism to process commands such as registration, login, logout,
 * and others by mapping command strings to specific handler methods. It communicates
 * with underlying services to execute these commands and provides feedback to the user
 * through the terminal service.
 * <p>
 * The {@code CommandHandler} relies on the following services:
 * - {@code RestApiService} for interactions with a REST API to handle user authentication and account management.
 * - {@code WebSocketService} for establishing and closing WebSocket connections.
 * - {@code TerminalService} for displaying messages and interacting with the user interface.
 * <p>
 * Commands:
 * - "register": Registers a new user.
 * - "unregister": Unregisters the current user.
 * - "login": Logs in the user.
 * - "logout": Logs out the current user.
 * - "clear": Clears the terminal.
 * - "exit": Terminates the application.
 * <p>
 * The class also supports error handling for invalid or unrecognized commands.
 */
public class CommandHandler {
    private final RestApiService restApiService;
    private final WebSocketService webSocketService;
    private final TerminalService terminalService;
    private final UserService userService;
    private final Map<String, Function<String[], Boolean>> commands = new HashMap<>();

    public CommandHandler(RestApiService restApiService,
                          WebSocketService webSocketService,
                          TerminalService terminalService,
                          UserService userService) {
        this.restApiService = restApiService;
        this.webSocketService = webSocketService;
        this.terminalService = terminalService;
        this.userService = userService;

        init();
    }

    private void init() {
        commands.put("help", this::help);
        commands.put("register", this::register);
        commands.put("unregister", this::unregister);
        commands.put("logout", this::logout);
        commands.put("login", this::login);
        commands.put("invitecode", this::inviteCode);
        commands.put("invite", this::invite);
        commands.put("accept", this::accept);
        commands.put("reject", this::reject);
        commands.put("disconnect", this::disconnect);
        commands.put("connections", this::connections);
        commands.put("pending", this::pending);
        commands.put("create", this::create);
        commands.put("enter", this::enter);
        commands.put("clear", this::clear);
        commands.put("exit", this::exit);
    }

    /**
     * Processes a user command and dispatches it to the appropriate service for processing.
     *
     * @param command  The command string to be processed. Must be a valid command.
     * @param argument The optional argument string associated with the command. Cannot be empty.
     * @return false if the command is logout, otherwise true.
     */
    public boolean process(String command, String argument) {
        Function<String[], Boolean> commander = commands.getOrDefault(command, (ignored) -> {
            terminalService.printSystemMessage("Invalid command: %s".formatted(command));
            return true;
        });
        return commander.apply(argument.split(" "));
    }

    private Boolean register(String[] params) {
        if (userService.isInLobby() && params.length > 1) {
            if (restApiService.register(params[0], params[1])) {
                terminalService.printSystemMessage("Registered.");
            } else {
                terminalService.printSystemMessage("Failed to register.");
            }
        }
        return true;
    }

    private Boolean unregister(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.closeSession();
            if (restApiService.unregister()) {
                terminalService.printSystemMessage("Unregistered.");
            } else {
                terminalService.printSystemMessage("Failed to unregister.");
            }
        }
        return true;
    }

    private Boolean login(String[] params) {
        if (userService.isInLobby() && params.length > 1) {
            if (restApiService.login(params[0], params[1])) {
                if (webSocketService.createSession(restApiService.getSessionId())) {
                    userService.login(params[0]);
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
            userService.logout();
            terminalService.printSystemMessage("Logged out.");
        } else {
            terminalService.printSystemMessage("Failed to logout.");
        }
        return true;
    }

    private Boolean inviteCode(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchUserInviteCodeSendMessage());
            terminalService.printSystemMessage("Fetching invite code for mine. Please wait...");
        }
        return true;
    }

    private Boolean invite(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new InviteSendMessage(params[0]));
            terminalService.printSystemMessage("Inviting %s. Please wait...".formatted(params[0]));
        }
        return true;
    }

    private Boolean accept(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new AcceptSendMessage(params[0]));
            terminalService.printSystemMessage("Accepting invite for %s. Please wait...".formatted(params[0]));
        }
        return true;
    }

    private Boolean reject(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new RejectSendMessage(params[0]));
            terminalService.printSystemMessage("Rejecting invite for %s. Please wait...".formatted(params[0]));
        }
        return true;
    }

    private Boolean disconnect(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new DisconnectSendMessage(params[0]));
            terminalService.printSystemMessage("Disconnecting with %s. Please wait...".formatted(params[0]));
        }
        return true;
    }

    private Boolean connections(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchConnectionsSendMessage(UserConnectionStatus.ACCEPTED));
            terminalService.printSystemMessage("Fetching connections. Please wait...");
        }
        return true;
    }

    private Boolean pending(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchConnectionsSendMessage(UserConnectionStatus.PENDING));
            terminalService.printSystemMessage("Fetching pending connections. Please wait...");
        }
        return true;
    }

    private boolean create(String[] params) {
        if (userService.isInLobby() && params.length > 1) {
            webSocketService.sendMessage(new CreateChannelSendMessage(params[0], params[1]));
            terminalService.printSystemMessage("Creating channel %s. Please wait...".formatted(params[0]));
        }
        return true;
    }

    private boolean enter(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            try {
                long channelId = Long.parseLong(params[0]);
                webSocketService.sendMessage(new EnterChannelSendMessage(channelId));
                terminalService.printSystemMessage("Entering channel %s.".formatted(params[0]));
            } catch (NumberFormatException e) {
                terminalService.printSystemMessage("Invalid channel ID: %s".formatted(params[0]));
            }
        }
        return true;
    }

    private Boolean clear(String[] params) {
        terminalService.clearTerminal();
        return true;
    }

    private Boolean exit(String[] params) {
        login(params);
        terminalService.printSystemMessage("Exiting...");
        return false;
    }

    private Boolean help(String[] params) {
        terminalService.printSystemMessage("""
                Commands for Lobby:
                '/register': Registers a new user. ex: /register <username> <password>
                '/unregister': Unregisters the current user. ex: /unregister
                '/login': Logs in the user. ex: /login <username> <password>
                '/invitecode': Fetches the invite code for the current user. ex: /invitecode
                '/invite': Invites a user to the chat room. ex: /invite <invite code>
                '/accept': Accepts an invite from a user. ex: /accept <inviter username>
                '/reject': Rejects an invite from a user. ex: /reject <inviter username>
                '/disconnect': Disconnects with a user. ex: /disconnect <connected username>
                '/connections': Fetches the list of connected users. ex: /connections
                '/pending': Fetches the list of pending connections. ex: /pending
                '/create': Creates a new direct channel. ex: /create <channel title> <username>
                '/enter': Enters a direct channel. ex: /enter <channel id>
                
                Commands for Channel:
                
                
                Commands for Lobby/Channel:
                '/clear': Clears the terminal. ex: /clear
                '/exit': Terminates the application. ex: /exit
                '/logout': Logs out the current user. ex: /logout
                """);
        return true;
    }
}
