package cwchoiit.chat.server.handler.request;

import cwchoiit.chat.server.constants.MessageType;

public class KeepAliveRequest extends BaseRequest {

    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
