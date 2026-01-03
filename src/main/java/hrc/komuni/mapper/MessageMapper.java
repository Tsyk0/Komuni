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
            "ORDER BY send_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Message> selectMessagesByConvId(
            @Param("convId") Long convId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM message " +
            "WHERE conv_id = #{convId} AND is_recalled = 0")
    Long countMessagesByConvId(@Param("convId") Long convId);

    // 修改：删除 read_time 参数
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
            "ORDER BY send_time DESC LIMIT 1")
    Message selectLastMessageByConvId(@Param("convId") Long convId);

    @Insert("INSERT INTO message (" +
            "conv_id, sender_id, receiver_id, message_type, message_content, " +
            "message_status, is_recalled, reply_to_message_id, at_user_ids, " +
            "conv_msg_seq, send_time" +  // ⬅️ 新增字段
            ") VALUES (" +
            "#{convId}, #{senderId}, #{receiverId}, #{messageType}, #{messageContent}, " +
            "#{messageStatus}, #{isRecalled}, #{replyToMessageId}, " +
            "#{atUserIds, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, " +
            "#{convMsgSeq}, #{sendTime}" +  // ⬅️ 新增字段
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insertMessage(Message message);

    // 需要添加的方法：按序列号查询
    @Select("SELECT * FROM message " +
            "WHERE conv_id = #{convId} AND conv_msg_seq > #{lastSeq} " +
            "AND is_recalled = 0 " +
            "ORDER BY conv_msg_seq ASC " +
            "LIMIT #{limit}")
    List<Message> selectNewMessagesBySeq(
            @Param("convId") Long convId,
            @Param("lastSeq") Long lastSeq,
            @Param("limit") Integer limit);
}