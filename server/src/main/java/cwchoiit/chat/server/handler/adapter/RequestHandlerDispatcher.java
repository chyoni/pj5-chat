package cwchoiit.chat.server.handler.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RequestHandlerDispatcher {

    private final Map<String, RequestHandler> handlers = new HashMap<>();

    public RequestHandlerDispatcher(List<RequestHandler> requestHandlers) {
        for (RequestHandler requestHandler : requestHandlers) {
            handlers.put(requestHandler.messageType(), requestHandler);
        }
    }

    public Optional<RequestHandler> findHandler(String messageType) {
        return Optional.ofNullable(handlers.get(messageType));
    }
}
