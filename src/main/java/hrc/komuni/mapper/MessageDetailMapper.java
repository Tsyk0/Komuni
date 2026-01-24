package hrc.komuni.mapper;

import hrc.komuni.dto.MessageDetailDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageDetailMapper {

    @Select("SELECT m.message_id, " +
            "       m.conv_id, " +
            "       m.sender_id, " +
            "       m.message_type, " +
            "       m.message_content, " +
            "       m.message_status, " +
            "       m.is_recalled, " +
            "       m.send_time, " +
            "       u.user_avatar, " +
            "       cm.member_nickname, " +
            "       cm.private_display_name, " +
            "       c.conv_type, " +
            "       CASE WHEN #{currentUserId} = m.sender_id THEN true ELSE false END as is_sent_by_me, " +  // 移除：m.conv_msg_seq,
            "       CASE " +
            "           WHEN cm.member_nickname IS NOT NULL AND cm.member_nickname != '' THEN cm.member_nickname " +
            "           WHEN c.conv_type = 1 AND cm.private_display_name IS NOT NULL AND cm.private_display_name != '' THEN cm.private_display_name " +
            "           WHEN #{currentUserId} = m.sender_id THEN u.user_nickname " +
            "           ELSE u.user_nickname " +
            "       END as display_name " +
            "FROM message m " +
            "LEFT JOIN user u ON m.sender_id = u.user_id " +
            "LEFT JOIN conversation_member cm ON m.conv_id = cm.conv_id " +
            "    AND m.sender_id = cm.user_id " +
            "    AND cm.member_status = 1 " +
            "LEFT JOIN conversation c ON m.conv_id = c.conv_id " +
            "WHERE m.conv_id = #{convId} " +
            "    AND m.is_recalled = 0 " +
            "ORDER BY m.send_time ASC " +  // 改为按时间排序
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "messageId", column = "message_id"),
            @Result(property = "convId", column = "conv_id"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "messageType", column = "message_type"),
            @Result(property = "messageContent", column = "message_content"),
            @Result(property = "messageStatus", column = "message_status"),
            @Result(property = "isRecalled", column = "is_recalled"),
            @Result(property = "sendTime", column = "send_time"),
            @Result(property = "senderAvatar", column = "user_avatar"),
            @Result(property = "memberNickname", column = "member_nickname"),
            @Result(property = "privateDisplayName", column = "private_display_name"),
            @Result(property = "convType", column = "conv_type"),
            // 移除：@Result(property = "convMsgSeq", column = "conv_msg_seq"),
            @Result(property = "isSentByMe", column = "is_sent_by_me"),
            @Result(property = "displayName", column = "display_name")
    })
    List<MessageDetailDTO> selectMessageDetailsByConvId(
            @Param("convId") Long convId,
            @Param("currentUserId") Long currentUserId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
}