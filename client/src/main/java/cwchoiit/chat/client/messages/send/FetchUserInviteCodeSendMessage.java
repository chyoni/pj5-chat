package cwchoiit.chat.client.messages.send;

import cwchoiit.chat.client.messages.BaseSendMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;

@Getter
@ToString
public class FetchUserInviteCodeSendMessage extends BaseSendMessage {

    public FetchUserInviteCodeSendMessage() {
        super(FETCH_USER_INVITE_CODE_REQUEST);
    }
}
