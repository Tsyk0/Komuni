package hrc.komuni.service;

import hrc.komuni.entity.MessageReadStatus;
import java.util.List;

public interface MessageReadStatusService {
    /**
     * 标记消息为已读
     */
    int markMessageAsRead(Long messageId, Long userId);

    /**
     * 批量标记消息为已读
     */
    int batchMarkMessagesAsRead(Long userId, List<Long> messageIds);

    /**
     * 查询消息的已读状态列表
     */
    List<MessageReadStatus> getReadStatusByMessageId(Long messageId);

    /**
     * 查询消息的已读用户数量
     */
    Integer getReadUserCount(Long messageId);

    /**
     * 查询用户已读的消息ID列表
     */
    List<Long> getReadMessageIdsByUserId(Long userId);
}