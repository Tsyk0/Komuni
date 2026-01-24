package hrc.komuni.mapper;

import hrc.komuni.entity.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMapper {

        @Select("SELECT * FROM conversation WHERE conv_id = #{convId}")
        Conversation selectConversationByConvId(@Param("convId") Long convId);

        @Select({
                "<script>",
                "SELECT * FROM conversation",
                "WHERE conv_id IN",
                "<foreach collection='convIds' item='id' open='(' separator=',' close=')'>",
                "   #{id}",
                "</foreach>",
                "AND conv_status = 1",
                "ORDER BY update_time DESC",
                "</script>"
        })
        List<Conversation> selectConversationsBatch(@Param("convIds") List<Long> convIds);

        @Select("SELECT conv_name FROM conversation WHERE conv_id = #{convId}")
        String getConvNameByConvId(@Param("convId") Long convId);

        @Select("SELECT private_display_name FROM conversation_member " +
                "WHERE conv_id = #{convId} AND user_id = #{userId}")
        String getPrivateDisplayName(@Param("convId") Long convId, @Param("userId") Long userId);

        @Select("SELECT conv_type FROM conversation WHERE conv_id = #{convId}")
        Integer getConvTypeByConvId(@Param("convId") Long convId);

        @Insert("INSERT INTO conversation (" +
                "conv_type, conv_name, conv_avatar, conv_description, conv_owner_id, " +
                "max_member_count, current_member_count, conv_status, enable_read_receipt, create_time" +
                ") VALUES (" +
                "#{convType}, #{convName}, #{convAvatar}, #{convDescription}, #{convOwnerId}, " +
                "#{maxMemberCount}, #{currentMemberCount}, #{convStatus}, #{enableReadReceipt}, #{createTime}" +
                ")")
        @Options(useGeneratedKeys = true, keyProperty = "convId")
        int insertConversation(Conversation conversation);

        // 移除：updateCurrentMsgSeq方法
        // @Update("UPDATE conversation SET current_msg_seq = #{currentMsgSeq}, update_time = NOW() WHERE conv_id = #{convId}")
        // int updateCurrentMsgSeq(@Param("convId") Long convId, @Param("currentMsgSeq") Long currentMsgSeq);

        @Update("UPDATE conversation SET current_member_count = current_member_count + 1 " +
                "WHERE conv_id = #{convId}")
        int incrementMemberCount(@Param("convId") Long convId);

        @Update("UPDATE conversation SET current_member_count = current_member_count - 1 " +
                "WHERE conv_id = #{convId}")
        int decrementMemberCount(@Param("convId") Long convId);

        @Select("SELECT COUNT(*) FROM conversation_member " +
                "WHERE conv_id = #{convId} AND member_status = 1")
        Integer getMemberCount(@Param("convId") Long convId);

        @Select("SELECT * FROM conversation " +
                "WHERE conv_id IN (" +
                "SELECT conv_id FROM conversation_member WHERE user_id = #{userId} AND member_status = 1" +
                ") ORDER BY update_time DESC")
        List<Conversation> selectConversationsByUserId(@Param("userId") Long userId);

        // 新增：更新群聊的已读回执设置
        @Update("UPDATE conversation SET enable_read_receipt = #{enableReadReceipt}, update_time = NOW() " +
                "WHERE conv_id = #{convId} AND conv_type = 2")
        int updateReadReceiptSetting(
                @Param("convId") Long convId,
                @Param("enableReadReceipt") Boolean enableReadReceipt);

        // 新增：查询群聊的已读回执设置
        @Select("SELECT enable_read_receipt FROM conversation WHERE conv_id = #{convId}")
        Boolean getReadReceiptSetting(@Param("convId") Long convId);

        @Select("SELECT " +
                "GREATEST( " +
                "   (SELECT COUNT(*) " +
                "    FROM message m " +
                "    WHERE m.conv_id = #{convId} " +
                "    AND m.send_time > COALESCE((" +
                "        SELECT MAX(m2.send_time) " +
                "        FROM conversation_member cm2 " +
                "        JOIN message m2 ON m2.message_id = cm2.last_read_message_id " +
                "        WHERE cm2.conv_id = #{convId} AND cm2.user_id = #{userId}" +
                "    ), '1970-01-01') " +  // 改为按时间比较
                "    AND m.sender_id != #{userId} " +
                "    AND m.is_recalled = 0 " +
                "    AND m.message_status >= 1), " +
                "   0" +
                ") as unread_count " +
                "FROM conversation c " +
                "JOIN conversation_member cm ON c.conv_id = cm.conv_id " +
                "WHERE c.conv_id = #{convId} AND cm.user_id = #{userId}")
        Integer getUnreadMessageCount(@Param("convId") Long convId, @Param("userId") Long userId);
}