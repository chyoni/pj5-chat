package cwchoiit.chat.server.service.response;

import cwchoiit.chat.server.entity.UserChannel;

public record ChannelParticipantResponse(Long userId) {

    public static ChannelParticipantResponse of(UserChannel userChannel) {
        return new ChannelParticipantResponse(userChannel.getUserId());
    }
}
