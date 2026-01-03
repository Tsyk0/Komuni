package hrc.komuni.service;

import hrc.komuni.entity.Message;
import java.util.List;

public interface MessageService {
    Message selectMessageByMessageId(Long messageId);

    Long insertMessage(Message message);

    List<Message> getMessagesByConvId(Long convId, Integer page, Integer pageSize);

    Long countMessagesByConvId(Long convId);

    // 修改：删除 readTime 参数
    int updateMessageStatus(Long messageId, Integer status);

    int recallMessage(Long messageId, Long userId);

    Message getLastMessageByConvId(Long convId);
}