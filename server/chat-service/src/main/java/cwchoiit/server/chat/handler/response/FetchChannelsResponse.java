package cwchoiit.server.chat.handler.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import cwchoiit.server.chat.service.response.ChannelReadResponse;
import lombok.Getter;

import java.util.List;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CHANNELS_RESPONSE;

@Getter
public class FetchChannelsResponse extends BaseResponse {

    private final List<ChannelReadResponse> channels;

    @JsonCreator
    public FetchChannelsResponse(List<ChannelReadResponse> channels) {
        super(FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
