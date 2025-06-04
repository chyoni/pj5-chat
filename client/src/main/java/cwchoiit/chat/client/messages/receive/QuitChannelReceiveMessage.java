package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.QUIT_CHANNEL_RESPONSE;

@Getter
@ToString
public class QuitChannelReceiveMessage extends BaseReceiveMessage {
    private final Long channelId;

    @JsonCreator
    public QuitChannelReceiveMessage(@JsonProperty("channelId") Long channelId) {
        super(QUIT_CHANNEL_RESPONSE);
        this.channelId = channelId;
    }
}
