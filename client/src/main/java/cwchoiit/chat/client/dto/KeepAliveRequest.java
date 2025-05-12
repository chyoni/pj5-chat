package cwchoiit.chat.client.dto;

import cwchoiit.chat.client.constants.MessageType;

public class KeepAliveRequest extends BaseRequest {

    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
