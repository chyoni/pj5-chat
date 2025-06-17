package cwchoiit.server.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;


@Getter
@Entity
@ToString
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "connection_invite_code", nullable = false)
    private String connectionInviteCode;

    @Column(name = "connection_count", nullable = false)
    private int connectionCount;

    public static User create(String username, String password) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.connectionInviteCode = UUID.randomUUID().toString().replaceAll("-", "");
        return user;
    }

    public void changeConnectionCount(int count) {
        this.connectionCount = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
