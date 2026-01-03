package hrc.komuni.service.impl;

import hrc.komuni.entity.Message;
import hrc.komuni.mapper.MessageMapper;
import hrc.komuni.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;

    @Override
    public Message selectMessageByMessageId(Long messageId) {
        return messageMapper.selectMessageByMessageId(messageId);
    }

    @Override
    public Long insertMessage(Message message) {
        if (message.getSendTime() == null) {
            message.setSendTime(new Date());
        }
        if (message.getMessageStatus() == null) {
            message.setMessageStatus(1); // 默认已发送
        }
        if (message.getIsRecalled() == null) {
            message.setIsRecalled(false);
        }

        int result = messageMapper.insertMessage(message);
        if (result > 0) {
            return message.getMessageId();
        }
        return null;
    }

    @Override
    public List<Message> getMessagesByConvId(Long convId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        return messageMapper.selectMessagesByConvId(convId, offset, pageSize);
    }

    @Override
    public Long countMessagesByConvId(Long convId) {
        return messageMapper.countMessagesByConvId(convId);
    }

    // 修改：删除 readTime 参数
    @Override
    public int updateMessageStatus(Long messageId, Integer status) {
        return messageMapper.updateMessageStatus(messageId, status);
    }

    @Override
    public int recallMessage(Long messageId, Long userId) {
        return messageMapper.recallMessage(messageId, userId);
    }

    @Override
    public Message getLastMessageByConvId(Long convId) {
        return messageMapper.selectLastMessageByConvId(convId);
    }
}