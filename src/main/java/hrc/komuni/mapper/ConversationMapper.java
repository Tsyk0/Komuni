package hrc.komuni.mapper;

import hrc.komuni.entity.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMapper {

        @Select("SELECT * FROM conversation WHERE conv_id = #{convId}")
        Conversation selectConversationByConvId(@Param("convId") Long convId);

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


        @Update("UPDATE conversation SET " +
                        "conv_name = #{convName}, conv_avatar = #{convAvatar}, " +
                        "conv_description = #{convDescription}, update_time = NOW() " +
                        "WHERE conv_id = #{convId}")
        int updateConversation(Conversation conversation);

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
                "   (COALESCE(c.current_msg_seq, 0) - COALESCE(cm.last_read_msg_seq, 0)) - " +
                "   COALESCE(( " +
                "       SELECT COUNT(*) " +
                "       FROM message m2 " +
                "       WHERE m2.conv_id = #{convId} " +
                "         AND m2.sender_id = #{userId} " +
                "         AND m2.conv_msg_seq > COALESCE(cm.last_read_msg_seq, 0) " +
                "         AND m2.message_status IN (1, 2, 3) " +
                "         AND m2.is_recalled = 0 " +
                "   ), 0), " +
                "   0" +
                ") as unread_count " +  // ⬅️ 必须加别名！
                "FROM conversation c " +
                "JOIN conversation_member cm ON c.conv_id = cm.conv_id " +
                "WHERE c.conv_id = #{convId} AND cm.user_id = #{userId}")
        Integer getUnreadMessageCount(@Param("convId") Long convId, @Param("userId") Long userId);
}