package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.NOTIFY_CHANNEL_JOIN;

@Getter
@ToString
public class ChannelJoinNotificationResponse extends BaseResponse {
    private final Long channelId;
    private final String title;

    public ChannelJoinNotificationResponse(Long channelId, String title) {
        super(NOTIFY_CHANNEL_JOIN);
        this.channelId = channelId;
        this.title = title;
    }
}
