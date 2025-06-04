package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.LEAVE_CHANNEL_RESPONSE;

@Getter
@ToString
public class LeaveChannelReceiveMessage extends BaseReceiveMessage {

    @JsonCreator
    public LeaveChannelReceiveMessage() {
        super(LEAVE_CHANNEL_RESPONSE);
    }
}
