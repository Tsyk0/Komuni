package hrc.komuni.mapper;

import hrc.komuni.entity.ConversationMember;
import hrc.komuni.entity.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMemberMapper {

    // 1. 查询相关方法
    @Select("SELECT * FROM conversation_member " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    ConversationMember selectByConvIdAndUserId(
            @Param("convId") Long convId,
            @Param("userId") Long userId
    );

    @Select("SELECT * FROM conversation_member WHERE conv_id = #{convId} AND member_status = 1")
    List<ConversationMember> selectMembersByConvId(@Param("convId") Long convId);

    @Select("SELECT * FROM conversation_member WHERE user_id = #{userId} AND member_status = 1")
    List<ConversationMember> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT last_read_message_id FROM conversation_member WHERE conv_id = #{convId} AND user_id = #{userId}")
    Long getLastReadSeq(@Param("userId") Long userId,
                        @Param("convId") Long convId);

    @Select("SELECT private_display_name FROM conversation_member " +
            "WHERE conv_id = #{convId} AND user_id = #{userId} AND member_status = 1")
    String getPrivateDisplayName(@Param("convId") Long convId, @Param("userId") Long userId);

    @Select("SELECT DISTINCT conv_id FROM conversation_member WHERE user_id = #{userId} AND member_status = 1")
    List<Long> selectConvIdsByUserId(@Param("userId") Long userId);

    // 2. 插入相关方法
    @Insert("INSERT INTO conversation_member (" +
            "conv_id, user_id, " +
            "member_role, member_status, join_time" +
            ") VALUES (" +
            "#{convId}, #{userId}, " +
            "0, 1, NOW()" +  // member_role=0（普通成员），member_status=1（正常）
            ")")
    int insertConversationMember(@Param("convId") Long convId, @Param("userId") Long userId);

    // 3. 更新相关方法
    @Update("UPDATE conversation_member SET " +
            "member_nickname = #{memberNickname}, " +
            "member_role = #{memberRole}, " +
            "member_status = #{memberStatus}, " +
            "private_display_name = #{privateDisplayName}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateConversationMember(ConversationMember member);

    @Update("UPDATE conversation_member SET " +
            "private_display_name = #{displayName}, " +
            "update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int updatePrivateDisplayName(
            @Param("convId") Long convId,
            @Param("userId") Long userId,
            @Param("displayName") String displayName
    );

    @Update("UPDATE conversation_member SET " +
            "last_read_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int updateLastReadTime(@Param("convId") Long convId, @Param("userId") Long userId);

    @Update("UPDATE conversation_member SET last_read_message_id  = #{seq}, last_read_time = NOW(), unread_count = 0 WHERE conv_id = #{convId} AND user_id = #{userId}")
    int updateLastReadSeq(@Param("userId") Long userId,
                          @Param("convId") Long convId,
                          @Param("seq") Long seq);

    @Update("UPDATE conversation_member SET unread_count = unread_count + 1, update_time = NOW() WHERE conv_id = #{convId} AND user_id != #{excludeUserId} AND member_status = 1")
    int incrementUnreadCount(@Param("convId") Long convId,
                             @Param("excludeUserId") Long excludeUserId);

    @Update("UPDATE conversation_member SET member_status = 0, update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int removeMember(@Param("convId") Long convId, @Param("userId") Long userId);

    @Update("UPDATE conversation SET current_member_count = current_member_count + 1 WHERE conv_id = #{convId}")
    int incrementMemberCount(@Param("convId") Long convId);

    @Update("UPDATE conversation SET current_member_count = current_member_count - 1 WHERE conv_id = #{convId} AND current_member_count > 0")
    int decrementMemberCount(@Param("convId") Long convId);

//    @Update("UPDATE conversation_member cm " +
//            "SET cm.unread_count = ( " +
//            "    SELECT COALESCE(MAX(m.conv_msg_seq) - ( " +
//            "        SELECT m2.conv_msg_seq FROM message m2 " +
//            "        WHERE m2.message_id = cm.last_read_message_id " +
//            "    ), 0) " +
//            "    FROM message m " +
//            "    WHERE m.conv_id = cm.conv_id " +
//            "    AND m.message_status < 3 " +  // 排除已读状态的消息
//            ") " +
//            "WHERE cm.conv_id = #{convId} AND cm.user_id = #{userId}")
//    int updateUnreadCountBasedOnLastRead(@Param("convId") Long convId,
//                                         @Param("userId") Long userId);
@Update("UPDATE conversation_member cm " +
        "JOIN (" +
        "    SELECT m.conv_id, " +
        "           COUNT(*) as unread_count " +
        "    FROM message m " +
        "    WHERE m.conv_id = #{convId} " +
        "    AND m.conv_msg_seq > COALESCE((" +
        "        SELECT m2.conv_msg_seq FROM conversation_member cm2 " +
        "        JOIN message m2 ON m2.message_id = cm2.last_read_message_id " +
        "        WHERE cm2.conv_id = #{convId} AND cm2.user_id = #{userId}" +
        "    ), 0) " +
        "    AND m.sender_id != #{userId} " +
        "    AND m.is_recalled = 0 " +
        "    AND m.message_status >= 1 " +
        "    GROUP BY m.conv_id" +
        ") unread_stats ON unread_stats.conv_id = cm.conv_id " +
        "SET cm.unread_count = COALESCE(unread_stats.unread_count, 0), " +
        "    cm.update_time = NOW() " +
        "WHERE cm.conv_id = #{convId} AND cm.user_id = #{userId}")
int updateUnreadCount(@Param("convId") Long convId,
                      @Param("userId") Long userId);
    @Update("UPDATE conversation_member cm " +
            "JOIN ( " +
            "    SELECT conv_id, MAX(conv_msg_seq) as max_seq " +
            "    FROM message " +
            "    WHERE conv_id = #{convId} " +
            "    AND message_status < 3 " +
            "    GROUP BY conv_id " +
            ") latest_msg ON cm.conv_id = latest_msg.conv_id " +
            "SET cm.unread_count = latest_msg.max_seq - COALESCE( " +
            "    (SELECT conv_msg_seq FROM message WHERE message_id = cm.last_read_message_id), 0) " +
            "WHERE cm.conv_id = #{convId} AND cm.member_status = 1")
    int updateAllMembersUnreadCount(@Param("convId") Long convId);

    // 4. 计算相关方法
// 直接将未读计数设置为0（标记为已读）
    @Update("UPDATE conversation_member " +
            "SET unread_count = 0, " +
            "    last_read_time = NOW(), " +
            "    last_read_message_id = (" +
            "        SELECT message_id FROM message " +
            "        WHERE conv_id = #{convId} " +
            "        ORDER BY conv_msg_seq DESC LIMIT 1" +
            "    ), " +
            "    update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int setUnreadCountZero(@Param("convId") Long convId,
                           @Param("userId") Long userId);

    // 5. 会话创建相关方法
    @Insert("INSERT INTO conversation (conv_type, conv_status, max_member_count, current_member_count, enable_read_receipt, create_time) " +
            "VALUES (1, 1, 2, 0, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "convId")
    int createSingleConversation(Conversation conversation);

    @Insert("INSERT INTO conversation (conv_type, conv_name, conv_owner_id, conv_status, max_member_count, current_member_count, enable_read_receipt, create_time) " +
            "VALUES (2, #{convName}, #{ownerId}, 1, 500, 0, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "convId")
    int createGroupConversation(@Param("convName") String convName, @Param("ownerId") Long ownerId);

    // ============ 未在Service中使用的方法（放在底端） ============

    /**
     * 查询会话信息
     */
    // 此方法被注释掉了，未使用
}