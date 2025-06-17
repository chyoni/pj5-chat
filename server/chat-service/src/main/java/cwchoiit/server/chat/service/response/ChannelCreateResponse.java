package cwchoiit.server.chat.service.response;

public record ChannelCreateResponse(Long channelId, String title, int headCount) {

    public static ChannelCreateResponse directChannel(Long channelId, String title) {
        return new ChannelCreateResponse(channelId, title, 2);
    }

    public static ChannelCreateResponse groupChannel(Long channelId, String title, int headCount) {
        return new ChannelCreateResponse(channelId, title, headCount);
    }
}
