package hrc.komuni.service.impl;

import hrc.komuni.entity.Message;
import hrc.komuni.mapper.ConversationMemberMapper;
import hrc.komuni.mapper.MessageMapper;
import hrc.komuni.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;

    @Autowired
    ConversationMemberMapper conversationMemberMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // 新增

    @Override
    public Message selectMessageByMessageId(Long messageId) {
        return messageMapper.selectMessageByMessageId(messageId);
    }

    @Override
    @Transactional
    public Long insertMessage(Message message) {
        // 1. 生成序列号
        Long seq = getNextSeq(message.getConvId());
        message.setConvMsgSeq(seq);

        // 2. 设置默认值
        if (message.getMessageStatus() == null) {
            message.setMessageStatus(0);
        }
        if (message.getIsRecalled() == null) {
            message.setIsRecalled(false);
        }

        // 3. 保存消息
        int result = messageMapper.insertMessage(message);

        // 4. 更新未读数
        if (result > 0) {
            conversationMemberMapper.incrementUnreadCount(
                    message.getConvId(),
                    message.getSenderId()
            );
        }

        return message.getMessageId();
    }

    /**
     * 私有方法：获取下一个序列号
     */
    @Transactional
    public synchronized Long getNextSeq(Long convId) {
        try {
            Long currentSeq = jdbcTemplate.queryForObject(
                    "SELECT current_msg_seq FROM conversation WHERE conv_id = ? FOR UPDATE",
                    Long.class, convId
            );

            if (currentSeq == null) {
                currentSeq = 0L;
            }

            Long nextSeq = currentSeq + 1;

            jdbcTemplate.update(
                    "UPDATE conversation SET current_msg_seq = ? WHERE conv_id = ?",
                    nextSeq, convId
            );

            return nextSeq;

        } catch (Exception e) {
            // 创建记录并返回1
            jdbcTemplate.update(
                    "INSERT INTO conversation (conv_id, current_msg_seq) VALUES (?, 1) " +
                            "ON DUPLICATE KEY UPDATE current_msg_seq = 1",
                    convId
            );
            return 1L;
        }
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