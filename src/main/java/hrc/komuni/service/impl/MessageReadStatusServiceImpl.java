package hrc.komuni.service.impl;

import hrc.komuni.entity.MessageReadStatus;
import hrc.komuni.mapper.MessageReadStatusMapper;
import hrc.komuni.service.MessageReadStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageReadStatusServiceImpl implements MessageReadStatusService {

    @Autowired
    private MessageReadStatusMapper messageReadStatusMapper;

    @Override
    public int markMessageAsRead(Long messageId, Long userId) {
        MessageReadStatus readStatus = new MessageReadStatus();
        readStatus.setMessageId(messageId);
        readStatus.setUserId(userId);
        readStatus.setReadTime(new Date());
        return messageReadStatusMapper.insertOrUpdateReadStatus(readStatus);
    }

    @Override
    public int batchMarkMessagesAsRead(Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        return messageReadStatusMapper.batchInsertReadStatus(userId, messageIds);
    }

    @Override
    public List<MessageReadStatus> getReadStatusByMessageId(Long messageId) {
        return messageReadStatusMapper.selectReadStatusByMessageId(messageId);
    }

    @Override
    public Integer getReadUserCount(Long messageId) {
        return messageReadStatusMapper.countReadUsersByMessageId(messageId);
    }

    @Override
    public List<Long> getReadMessageIdsByUserId(Long userId) {
        return messageReadStatusMapper.selectReadMessageIdsByUserId(userId);
    }
}