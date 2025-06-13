package cwchoiit.chat.server.service;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static cwchoiit.chat.server.constants.MessageType.INVITE_RESPONSE;
import static cwchoiit.chat.server.constants.MessageType.MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Service - PushService")
@ExtendWith(MockitoExtension.class)
class PushServiceTest {

    LogCaptor logCaptor;
    @InjectMocks
    private PushService pushService;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(PushService.class);
    }

    @AfterEach
    void tearDown() {
        logCaptor.close();
    }

    @Test
    @DisplayName("푸시 메시지에 해당하는 타입인 경우 푸시 메시지가 발송된다.")
    void push() {
        pushService.registerPushMessageType(MESSAGE);
        pushService.pushMessage(1L, MESSAGE, "Message");

        assertThat(logCaptor.getInfoLogs()).anyMatch(infoLog -> infoLog.contains("Push message : "));
    }

    @Test
    @DisplayName("푸시 메시지에 해당하는 타입이 아닌 경우 푸시 메시지가 발송되지 않는다.")
    void push_not() {
        pushService.registerPushMessageType(INVITE_RESPONSE);
        pushService.pushMessage(1L, MESSAGE, "Message");

        assertThat(logCaptor.getInfoLogs()).isEmpty();
    }

}