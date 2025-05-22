package cwchoiit.chat.client.service;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserService {

    private enum Location {
        LOBBY,
        CHANNEL
    }

    private Location location = Location.LOBBY;
    private String username;
    private Long channelId;

    public void login(String username) {
        this.username = username;
        moveToLobby();
    }

    public void logout() {
        this.username = null;
        moveToLobby();
    }

    public boolean isInLobby() {
        return location == Location.LOBBY;
    }

    public boolean isInChannel() {
        return location == Location.CHANNEL;
    }

    public void moveToChannel(Long channelId) {
        this.location = Location.CHANNEL;
        this.channelId = channelId;
    }

    public void moveToLobby() {
        this.location = Location.LOBBY;
        this.channelId = null;
    }
}
