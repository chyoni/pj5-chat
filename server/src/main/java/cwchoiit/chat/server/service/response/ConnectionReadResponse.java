package cwchoiit.chat.server.service.response;

import cwchoiit.chat.server.constants.UserConnectionStatus;

public record ConnectionReadResponse(String username, UserConnectionStatus status) {

}
