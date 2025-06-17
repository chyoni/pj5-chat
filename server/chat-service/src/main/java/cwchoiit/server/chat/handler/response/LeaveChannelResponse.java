package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.LEAVE_CHANNEL_RESPONSE;

@Getter
@ToString
public class LeaveChannelResponse extends BaseResponse {
    public LeaveChannelResponse() {
        super(LEAVE_CHANNEL_RESPONSE);
    }
}
