package cwchoiit.server.chat.handler.request;

import cwchoiit.server.chat.constants.MessageType;

public class KeepAliveRequest extends BaseRequest {

    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}
