package hrc.komuni.mapper;

import hrc.komuni.dto.ConversationDetailDTO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ConversationDetailMapper {

    /**
     * 简化版：分两步查询
     */

    // 第一步：获取基础会话信息
    @Select("SELECT " +
            "c.conv_id, " +
            "c.conv_type, " +
            "c.conv_name, " +
            "c.conv_avatar, " +
            "c.current_member_count, " +
            "c.max_member_count, " +
            "c.conv_status, " +
            "c.update_time, " +  // 移除：c.current_msg_seq,
            "cm.private_display_name, " +
            "cm.unread_count " +
            "FROM conversation c " +
            "INNER JOIN conversation_member cm ON c.conv_id = cm.conv_id " +
            "    AND cm.user_id = #{userId} " +
            "    AND cm.member_status = 1 " +
            "WHERE c.conv_status = 1 " +
            "ORDER BY c.update_time DESC")
    @Results({
            @Result(property = "convId", column = "conv_id"),
            @Result(property = "convType", column = "conv_type"),
            @Result(property = "convName", column = "conv_name"),
            @Result(property = "convAvatar", column = "conv_avatar"),
            @Result(property = "currentMemberCount", column = "current_member_count"),
            @Result(property = "maxMemberCount", column = "max_member_count"),
            @Result(property = "convStatus", column = "conv_status"),
            // 移除：@Result(property = "currentMsgSeq", column = "current_msg_seq"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "privateDisplayName", column = "private_display_name"),
            @Result(property = "unreadCount", column = "unread_count")
    })
    List<ConversationDetailDTO> selectBasicConversationsByUserId(@Param("userId") Long userId);

    // 第二步：获取会话的最后一条消息（可以在Service中循环调用）
    @Select("SELECT " +
            "m.message_id as last_message_id, " +
            "m.sender_id as last_sender_id, " +
            "m.message_type as last_message_type, " +
            "m.message_content as last_message_content, " +
            "m.send_time as last_send_time, " +
            "u.user_avatar as last_sender_avatar, " +
            "CASE " +
            "    WHEN c.conv_type = 2 AND cm.member_nickname IS NOT NULL AND cm.member_nickname != '' " +
            "        THEN cm.member_nickname " +
            "    WHEN c.conv_type = 1 AND cm.private_display_name IS NOT NULL AND cm.private_display_name != '' " +
            "        THEN cm.private_display_name " +
            "    ELSE u.user_nickname " +
            "END as last_sender_display_name " +
            "FROM message m " +
            "LEFT JOIN user u ON m.sender_id = u.user_id " +
            "LEFT JOIN conversation c ON m.conv_id = c.conv_id " +
            "LEFT JOIN conversation_member cm ON m.conv_id = cm.conv_id " +
            "    AND m.sender_id = cm.user_id " +
            "    AND cm.member_status = 1 " +
            "WHERE m.conv_id = #{convId} " +
            "    AND m.is_recalled = 0 " +
            "    AND m.send_time = ( " +  // 改为按时间查询，而不是conv_msg_seq
            "        SELECT MAX(send_time) " +
            "        FROM message " +
            "        WHERE conv_id = #{convId} AND is_recalled = 0" +
            "    ) " +
            "LIMIT 1")
    ConversationDetailDTO.LastMessageInfo selectLastMessageByConvId(@Param("convId") Long convId);
}