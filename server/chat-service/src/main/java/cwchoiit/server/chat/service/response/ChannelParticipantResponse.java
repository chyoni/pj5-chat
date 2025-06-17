package cwchoiit.server.chat.service.response;

import cwchoiit.server.chat.entity.UserChannel;

public record ChannelParticipantResponse(Long userId) {

    public static ChannelParticipantResponse of(UserChannel userChannel) {
        return new ChannelParticipantResponse(userChannel.getUserId());
    }
}
