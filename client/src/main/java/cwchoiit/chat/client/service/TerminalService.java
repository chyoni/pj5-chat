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

    public String readLine(String prompt) {
        String input = lineReader.readLine(prompt);
        terminal.puts(InfoCmp.Capability.cursor_up);
        terminal.puts(InfoCmp.Capability.delete_line);
        terminal.flush();
        return input;
    }

    public void printMessage(String username, String content) {
        lineReader.printAbove(String.format("%s: %s", username, content));
    }

    public void printSystemMessage(String content) {
        lineReader.printAbove(String.format("System: %s", content));
    }

    public void clearTerminal() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }
}
