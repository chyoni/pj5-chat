package cwchoiit.chat.push.handler;

import cwchoiit.chat.push.messages.BaseRecord;

public interface RecordHandler {
    String messageType();

    void handle(BaseRecord baseRecord);
}
