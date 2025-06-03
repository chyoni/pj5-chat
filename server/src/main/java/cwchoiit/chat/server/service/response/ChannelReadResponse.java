package cwchoiit.chat.server.service.response;

import cwchoiit.chat.server.repository.projection.ChannelInformation;

public record ChannelReadResponse(Long channelId, String title, int headCount) {

    public static ChannelReadResponse of(ChannelInformation channelInformation) {
        return new ChannelReadResponse(
                channelInformation.getChannelId(),
                channelInformation.getTitle(),
                channelInformation.getHeadCount()
        );
    }
}
