package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CHANNELS_REQUEST;

@Getter
public class FetchChannelsRequest extends BaseRequest {

    @JsonCreator
    public FetchChannelsRequest() {
        super(FETCH_CHANNELS_REQUEST);
    }
}
