package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.ENTER_CHANNEL_RESPONSE;

@Getter
@ToString
public class EnterChannelReceiveMessage extends BaseReceiveMessage {
    private final Long channelId;
    private final String title;

    @JsonCreator
    public EnterChannelReceiveMessage(@JsonProperty("channelId") Long channelId,
                                      @JsonProperty("title") String title) {
        super(ENTER_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
