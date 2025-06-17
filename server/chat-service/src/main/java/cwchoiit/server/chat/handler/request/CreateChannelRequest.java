package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

import static cwchoiit.server.chat.constants.MessageType.CHANNEL_CREATE_REQUEST;

@Getter
public class CreateChannelRequest extends BaseRequest {

    private final String title;
    private final List<String> participantUsernames;

    @JsonCreator
    public CreateChannelRequest(@JsonProperty("title") String title,
                                @JsonProperty("participantUsernames") List<String> participantUsernames) {
        super(CHANNEL_CREATE_REQUEST);
        this.title = title;
        this.participantUsernames = participantUsernames;
    }
}
