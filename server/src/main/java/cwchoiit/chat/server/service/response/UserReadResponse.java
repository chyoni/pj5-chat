package cwchoiit.chat.server.service.response;

import cwchoiit.chat.server.entity.User;
import cwchoiit.chat.server.repository.projection.UserIdWithName;

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

    public static UserReadResponse of(UserIdWithName userIdWithName) {
        return new UserReadResponse(userIdWithName.getUserId(), userIdWithName.getUsername());
    }
}
