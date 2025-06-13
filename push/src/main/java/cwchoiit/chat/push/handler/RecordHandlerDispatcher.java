package cwchoiit.chat.push.handler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RecordHandlerDispatcher {

    private final Map<String, RecordHandler> handlers = new HashMap<>();

    public RecordHandlerDispatcher(List<RecordHandler> recordHandlers) {
        for (RecordHandler recordHandler : recordHandlers) {
            handlers.put(recordHandler.messageType(), recordHandler);
        }
    }

    public Optional<RecordHandler> findHandler(String messageType) {
        return Optional.ofNullable(handlers.get(messageType));
    }
}
