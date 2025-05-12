package cwchoiit.chat.server.auth;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS, // Redis Session 사용 시 이 객체를 Json 으로 직렬화/역직렬화를 위해 이 애노테이션을 사용
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private String password;

    @JsonCreator
    public AppUserDetails(@JsonProperty("userId") Long userId,
                          @JsonProperty("username") String username,
                          @JsonProperty("password") String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void erasePassword() {
        this.password = "";
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AppUserDetails that = (AppUserDetails) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
