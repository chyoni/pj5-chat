package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.FETCH_USER_INVITE_CODE_RESPONSE;

@Getter
@ToString
public class FetchUserInviteCodeReceiveMessage extends BaseReceiveMessage {

    private final String inviteCode;

    @JsonCreator
    public FetchUserInviteCodeReceiveMessage(@JsonProperty("inviteCode") String inviteCode) {
        super(FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }
}
