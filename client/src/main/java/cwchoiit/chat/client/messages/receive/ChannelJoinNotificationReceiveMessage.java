package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.NOTIFY_CHANNEL_JOIN;

@Getter
@ToString
public class ChannelJoinNotificationReceiveMessage extends BaseReceiveMessage {
    private final Long channelId;
    private final String title;

    @JsonCreator
    public ChannelJoinNotificationReceiveMessage(@JsonProperty("channelId") Long channelId,
                                                 @JsonProperty("title") String title) {
        super(NOTIFY_CHANNEL_JOIN);
        this.channelId = channelId;
        this.title = title;
    }
}
