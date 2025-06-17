package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.LEAVE_CHANNEL_REQUEST;

@Getter
public class LeaveChannelRequest extends BaseRequest {

    @JsonCreator
    public LeaveChannelRequest() {
        super(LEAVE_CHANNEL_REQUEST);
    }
}
