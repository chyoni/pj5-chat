package cwchoiit.chat.push.handler;

import cwchoiit.chat.push.messages.AcceptNotificationReceiveMessage;
import cwchoiit.chat.push.messages.BaseRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static cwchoiit.chat.push.constants.MessageType.NOTIFY_ACCEPT;

@Slf4j
@Component
public class AcceptNotificationRecordHandler implements RecordHandler {
    @Override
    public String messageType() {
        return NOTIFY_ACCEPT;
    }

    @Override
    public void handle(BaseRecord baseRecord) {
        if (baseRecord instanceof AcceptNotificationReceiveMessage record) {
            log.info("[handle] {} to offline user with id = {}", record, record.userId());
        }
    }
}
