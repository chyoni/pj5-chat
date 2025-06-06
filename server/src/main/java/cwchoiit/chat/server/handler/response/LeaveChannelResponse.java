package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.LEAVE_CHANNEL_RESPONSE;

@Getter
@ToString
public class LeaveChannelResponse extends BaseResponse {
    public LeaveChannelResponse() {
        super(LEAVE_CHANNEL_RESPONSE);
    }
}
