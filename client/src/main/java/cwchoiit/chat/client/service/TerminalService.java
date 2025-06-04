package cwchoiit.chat.client.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * A utility class for managing terminal interactions.
 * This class provides methods to handle terminal input, display messages,
 * and manage terminal screen behavior effectively.
 * <p>
 * The class uses {@link Terminal} and
 * {@link LineReader} to interact with the terminal.
 * It initializes these components during the creation of the service.
 * <p>
 * Key functionalities include:
 * - Reading user input with prompt customization.
 * - Displaying formatted messages in the terminal.
 * - Managing terminal screen actions such as clearing the screen.
 * <p>
 * This class is designed to be instantiated using the static factory method {@code create()}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TerminalService {

    private Terminal terminal;
    private LineReader lineReader;

    public static TerminalService create() {
        TerminalService terminalService = new TerminalService();
        try {
            terminalService.terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
        } catch (IOException e) {
            System.err.println("Failed to create TerminalService: " + e.getMessage());
            System.exit(1);
        }

        terminalService.lineReader = LineReaderBuilder.builder()
                .terminal(terminalService.terminal)
                .variable(LineReader.HISTORY_FILE, Paths.get("./client/chathistory/history.txt"))
                .build();

        return terminalService;
    }

    /**
     * Reads a line of input from the terminal with a given prompt.
     * The method displays the prompt to the user, retrieves their input,
     * and then performs terminal manipulations to clear the prompt line from the terminal.
     *
     * @param prompt the prompt message to display to the user
     * @return the input line entered by the user
     */
    public String readLine(String prompt) {
        String input = lineReader.readLine(prompt);
        // 사용자가 [Enter the message: ]프롬프트에 'XXX' 라고 입력하면 입력 후 커서를 하나 올림
        terminal.puts(InfoCmp.Capability.cursor_up);
        // 현재 커서라인을 지움. 즉, 기존에 입력 프롬프트로 지저분한 라인 지워주는 것
        terminal.puts(InfoCmp.Capability.delete_line);
        flush();
        return input;
    }

    /**
     * Prints a user-specific message to the terminal.
     * This method formats a message with the specified username and content, then displays it
     * above the current terminal input line using the line reader.
     *
     * @param username the name of the user who is associated with the message
     * @param content  the content of the message to be displayed
     */
    public void printMessage(String username, String content) {
        lineReader.printAbove(String.format("%s: %s", username, content));
    }

    /**
     * Prints a system message to the terminal.
     * This method formats the provided content as a system message and displays it above the current terminal input line.
     *
     * @param content the message content to be formatted and displayed as a system message
     */
    public void printSystemMessage(String content) {
        lineReader.printAbove(String.format("System: %s", content));
    }

    /**
     * Clears the terminal screen by sending a clear screen control sequence to the terminal.
     */
    public void clearTerminal() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        flush();
    }

    public void flush() {
        if (terminal != null) {
            terminal.flush();
        }
    }
}
