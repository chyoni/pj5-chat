package cwchoiit.server.chat.service;

import cwchoiit.server.chat.entity.Message;
import cwchoiit.server.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCommandService {

    private final MessageRepository messageRepository;

    @Transactional
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }
}
