package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import cwchoiit.chat.client.service.response.ChannelReadResponse;
import lombok.Getter;

import java.util.List;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CHANNELS_RESPONSE;

@Getter
public class FetchChannelsReceiveMessage extends BaseReceiveMessage {

    private final List<ChannelReadResponse> channels;

    @JsonCreator
    public FetchChannelsReceiveMessage(@JsonProperty("channels") List<ChannelReadResponse> channels) {
        super(FETCH_CHANNELS_RESPONSE);
        this.channels = channels;
    }
}
