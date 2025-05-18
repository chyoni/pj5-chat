package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.CHANNEL_CREATE_REQUEST;

@Getter
public class CreateChannelRequest extends BaseRequest {

    private final String title;
    private final String participantUsername;

    @JsonCreator
    public CreateChannelRequest(@JsonProperty("title") String title,
                                @JsonProperty("participantUsername") String participantUsername) {
        super(CHANNEL_CREATE_REQUEST);
        this.title = title;
        this.participantUsername = participantUsername;
    }
}
