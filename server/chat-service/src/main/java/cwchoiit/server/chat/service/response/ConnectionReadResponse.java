package cwchoiit.server.chat.service.response;

import cwchoiit.server.chat.constants.UserConnectionStatus;

public record ConnectionReadResponse(String username, UserConnectionStatus status) {

}
