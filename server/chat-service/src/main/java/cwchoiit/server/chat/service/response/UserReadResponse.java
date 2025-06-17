package cwchoiit.server.chat.service.response;

import cwchoiit.server.chat.entity.User;
import cwchoiit.server.chat.repository.projection.ConnectionInformation;

import java.util.Objects;

public record UserReadResponse (Long userId, String username) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserReadResponse that = (UserReadResponse) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    public static UserReadResponse of(User user) {
        return new UserReadResponse(user.getUserId(), user.getUsername());
    }

    public static UserReadResponse of(ConnectionInformation connectionInformation) {
        return new UserReadResponse(connectionInformation.getUserId(), connectionInformation.getUsername());
    }
}
