package hrc.komuni.mapper;

import hrc.komuni.entity.MessageReadStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageReadStatusMapper {

    /**
     * 插入已读状态记录
     */
    @Insert("INSERT INTO message_read_status (message_id, user_id, read_time) " +
            "VALUES (#{messageId}, #{userId}, #{readTime}) " +
            "ON DUPLICATE KEY UPDATE read_time = #{readTime}")
    int insertOrUpdateReadStatus(MessageReadStatus readStatus);

    /**
     * 查询消息的已读状态列表
     */
    @Select("SELECT * FROM message_read_status WHERE message_id = #{messageId}")
    List<MessageReadStatus> selectReadStatusByMessageId(@Param("messageId") Long messageId);

    /**
     * 查询用户已读的消息ID列表
     */
    @Select("SELECT message_id FROM message_read_status WHERE user_id = #{userId}")
    List<Long> selectReadMessageIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询消息的已读用户数量
     */
    @Select("SELECT COUNT(*) FROM message_read_status WHERE message_id = #{messageId}")
    Integer countReadUsersByMessageId(@Param("messageId") Long messageId);

    /**
     * 批量插入已读状态（用于标记多条消息为已读）
     */
    @Insert("<script>" +
            "INSERT INTO message_read_status (message_id, user_id, read_time) VALUES " +
            "<foreach collection='messageIds' item='msgId' separator=','>" +
            "(#{msgId}, #{userId}, NOW())" +
            "</foreach>" +
            "ON DUPLICATE KEY UPDATE read_time = NOW()" +
            "</script>")
    int batchInsertReadStatus(
            @Param("userId") Long userId,
            @Param("messageIds") List<Long> messageIds);
}