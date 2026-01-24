package hrc.komuni.service.impl;

import hrc.komuni.entity.Message;
import hrc.komuni.mapper.ConversationMemberMapper;
import hrc.komuni.mapper.MessageMapper;
import hrc.komuni.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;  // 添加Date导入
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;

    @Autowired
    ConversationMemberMapper conversationMemberMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Message selectMessageByMessageId(Long messageId) {
        return messageMapper.selectMessageByMessageId(messageId);
    }

    @Override
    @Transactional
    public Long insertMessage(Message message) {
        // 验证必要的字段
        if (message.getConvId() == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (message.getSenderId() == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }
        if (message.getMessageType() == null || message.getMessageType().trim().isEmpty()) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
        if (message.getMessageContent() == null || message.getMessageContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }


        // 2. 设置默认值
        if (message.getMessageStatus() == null) {
            message.setMessageStatus(1); // 默认为已发送
        }
        if (message.getIsRecalled() == null) {
            message.setIsRecalled(false);
        }
        if (message.getSendTime() == null) {
            message.setSendTime(new Date());
        }

        // 3. 验证发送者是否在会话中
        validateSenderInConversation(message.getConvId(), message.getSenderId());

        // 4. 保存消息
        int result = messageMapper.insertMessage(message);

        // 5. 更新未读数（排除发送者自己）
        if (result > 0) {
            // 这里先使用原有的方法，稍后修复
            incrementUnreadCountForOtherMembers(message.getConvId(), message.getSenderId());
        }

        return message.getMessageId();
    }

    /**
     * 私有方法：获取下一个序列号
     */

    /**
     * 确保会话存在，如果不存在则创建默认会话
     */
    private void ensureConversationExists(Long convId) {
        try {
            // 检查会话是否存在
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM conversation WHERE conv_id = ?",
                    Integer.class, convId
            );

            if (count == null || count == 0) {
                // 会话不存在，创建默认会话
                // 这里需要确定会话类型，默认设为单聊(1)
                // 需要设置所有NOT NULL字段
                jdbcTemplate.update(
                        "INSERT INTO conversation (conv_id, conv_type, conv_name, conv_status) VALUES (?, 1, '默认会话', 1)",
                        convId
                );
                System.out.println("创建默认会话: convId=" + convId);
            }
        } catch (Exception e) {
            throw new RuntimeException("检查会话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证发送者是否在会话中
     */
    private void validateSenderInConversation(Long convId, Long senderId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM conversation_member WHERE conv_id = ? AND user_id = ? AND member_status = 1",
                    Integer.class, convId, senderId
            );

            if (count == null || count == 0) {
                throw new IllegalArgumentException("发送者不在该会话中或无发言权限");
            }
        } catch (Exception e) {
            throw new RuntimeException("验证发送者权限失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为其他成员增加未读计数（排除发送者自己）
     */
    private void incrementUnreadCountForOtherMembers(Long convId, Long senderId) {
        try {
            // 使用JdbcTemplate直接执行更新
            int updated = jdbcTemplate.update(
                    "UPDATE conversation_member SET unread_count = unread_count + 1 " +
                            "WHERE conv_id = ? AND user_id != ? AND member_status = 1",
                    convId, senderId
            );

            if (updated > 0) {
                System.out.println("更新了 " + updated + " 个成员的未读计数");
            }
        } catch (Exception e) {
            System.err.println("更新未读计数失败: " + e.getMessage());
            // 这里不抛出异常，避免影响消息发送
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