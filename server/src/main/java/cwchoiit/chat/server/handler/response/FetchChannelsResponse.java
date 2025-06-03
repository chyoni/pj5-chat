package cwchoiit.chat.server.handler.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import cwchoiit.chat.server.service.response.ChannelReadResponse;
import lombok.Getter;

import java.util.List;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNELS_RESPONSE;

@Getter
public class FetchChannelsResponse extends BaseResponse {

    private final List<ChannelReadResponse> channels;

    @JsonCreator
    public FetchChannelsResponse(List<ChannelReadResponse> channels) {
        super(FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
