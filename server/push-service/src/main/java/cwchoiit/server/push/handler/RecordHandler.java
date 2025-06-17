package cwchoiit.server.push.handler;

import cwchoiit.server.push.messages.BaseRecord;

public interface RecordHandler {
    String messageType();

    void handle(BaseRecord baseRecord);
}
