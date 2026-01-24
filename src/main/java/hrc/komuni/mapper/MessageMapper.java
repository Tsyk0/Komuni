package hrc.komuni.mapper;

import hrc.komuni.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("SELECT * FROM message WHERE message_id = #{messageId}")
    Message selectMessageByMessageId(@Param("messageId") Long messageId);

    @Select("SELECT * FROM message " +
            "WHERE conv_id = #{convId} AND is_recalled = 0 " +
            "ORDER BY send_time ASC " +  // 改为按时间排序
            "LIMIT #{offset}, #{limit}")
    List<Message> selectMessagesByConvId(
            @Param("convId") Long convId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM message " +
            "WHERE conv_id = #{convId} AND is_recalled = 0")
    Long countMessagesByConvId(@Param("convId") Long convId);

    @Update("UPDATE message SET message_status = #{status} " +
            "WHERE message_id = #{messageId}")
    int updateMessageStatus(
            @Param("messageId") Long messageId,
            @Param("status") Integer status);

    @Update("UPDATE message SET is_recalled = 1, recall_time = NOW() " +
            "WHERE message_id = #{messageId} AND sender_id = #{userId}")
    int recallMessage(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId);

    @Select("SELECT * FROM message " +
            "WHERE conv_id = #{convId} AND is_recalled = 0 " +
            "ORDER BY send_time DESC LIMIT 1")  // 改为按时间排序
    Message selectLastMessageByConvId(@Param("convId") Long convId);

    @Insert("INSERT INTO message (" +
            "conv_id, sender_id, message_type, message_content, " +
            "message_status, is_recalled, reply_to_message_id, at_user_ids, " +
            "send_time" +  // 移除：conv_msg_seq,
            ") VALUES (" +
            "#{convId}, #{senderId}, #{messageType}, #{messageContent}, " +
            "#{messageStatus}, #{isRecalled}, #{replyToMessageId}, " +
            "#{atUserIds, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, " +
            "#{sendTime}" +  // 移除：#{convMsgSeq},
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insertMessage(Message message);

    @Select("SELECT * FROM message " +
            "WHERE conv_id = #{convId} AND send_time > (" +  // 改为按时间查询新消息
            "    SELECT MAX(send_time) FROM message " +
            "    WHERE conv_id = #{convId} AND message_id = #{lastMessageId}" +
            ") " +
            "AND is_recalled = 0 " +
            "ORDER BY send_time ASC " +
            "LIMIT #{limit}")
    List<Message> selectNewMessagesByTime(
            @Param("convId") Long convId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("limit") Integer limit);
}