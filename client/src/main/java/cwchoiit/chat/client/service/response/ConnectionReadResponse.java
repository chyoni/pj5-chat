package cwchoiit.chat.client.service.response;

import cwchoiit.chat.client.constants.UserConnectionStatus;

public record ConnectionReadResponse(String username, UserConnectionStatus status) {

}
