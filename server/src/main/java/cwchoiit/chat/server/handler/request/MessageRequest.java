package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.server.constants.MessageType;
import lombok.Getter;

@Getter
public class MessageRequest extends BaseRequest {
    private final String username;
    private final String content;
    private final Long channelId;

    /**
     * {@link MessageRequest} 생성자.
     * <p>
     * 여기서 {@code @JsonCreator}, {@code @JsonProperty} 애노테이션이 필요한 이유는, Jackson 라이브러리를 사용할 때
     * 기본 생성자, Setter 기반 직렬화, 역직렬화를 사용한다면 없어도 된다. 그러나, 현재 이 클래스는
     * 필드가 전부 final이 붙어 있는 상태고 Setter는 존재하지 않는다. 다른 생성자 또한 없다.
     * <p>
     * 이 경우엔 생성자 기반으로 역직렬화하겠다는 힌트를 Jackson 에게 알려줘야 어떤 JSON 필드가 어떤 파라미터에 매핑되는지 알 수 있고
     * 그렇기 때문에 사용하는 애노테이션이다.
     *
     * @param channelId the channel id to be sent.
     * @param username  the sender's username.
     * @param content   the message content to be sent.
     */
    @JsonCreator
    public MessageRequest(@JsonProperty("channelId") Long channelId,
                          @JsonProperty("username") String username,
                          @JsonProperty("content") String content) {
        super(MessageType.MESSAGE);
        this.channelId = channelId;
        this.username = username;
        this.content = content;
    }
}
