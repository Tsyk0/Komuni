package hrc.komuni.mapper;

import hrc.komuni.entity.ConversationMember;
import hrc.komuni.entity.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMemberMapper {

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

    @Insert("INSERT INTO conversation_member (" +
            "conv_id, user_id, member_nickname, member_role, member_status, " +
            "private_display_name, join_time" +
            ") VALUES (" +
            "#{convId}, #{userId}, #{memberNickname}, #{memberRole}, #{memberStatus}, " +
            "#{privateDisplayName}, #{joinTime}" +
            ")")
    int insertConversationMember(ConversationMember member);

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

    @Update("UPDATE conversation_member SET member_status = 0, update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int removeMember(@Param("convId") Long convId, @Param("userId") Long userId);

    @Update("UPDATE conversation_member SET last_read_msg_seq = #{seq}, last_read_time = NOW(), unread_count = 0 WHERE conv_id = #{convId} AND user_id = #{userId}")
    int updateLastReadSeq(@Param("userId") Long userId,
                          @Param("convId") Long convId,
                          @Param("seq") Long seq);

    @Update("UPDATE conversation_member SET unread_count = unread_count + 1, update_time = NOW() WHERE conv_id = #{convId} AND user_id != #{excludeUserId} AND member_status = 1")
    int incrementUnreadCount(@Param("convId") Long convId,
                             @Param("excludeUserId") Long excludeUserId);

    @Select("SELECT last_read_msg_seq FROM conversation_member WHERE conv_id = #{convId} AND user_id = #{userId}")
    Long getLastReadSeq(@Param("userId") Long userId,
                        @Param("convId") Long convId);

    // ============ 新增的方法 ============

    /**
     * 在conversationMember表中查找所有含有对应UserId的ConvId
     */
    @Select("SELECT DISTINCT conv_id FROM conversation_member WHERE user_id = #{userId} AND member_status = 1")
    List<Long> selectConvIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取对应convId, userId的用户对该conv设置的别名
     */
    @Select("SELECT private_display_name FROM conversation_member " +
            "WHERE conv_id = #{convId} AND user_id = #{userId} AND member_status = 1")
    String getPrivateDisplayName(@Param("convId") Long convId, @Param("userId") Long userId);

    // ============ 新增的会话相关方法（替代ConversationService依赖） ============

    /**
     * 查询会话信息
     */
    @Select("SELECT * FROM conversation WHERE conv_id = #{convId}")
    Conversation selectConversationByConvId(@Param("convId") Long convId);

    /**
     * 创建单聊会话
     */
    @Insert("INSERT INTO conversation (conv_type, conv_status, max_member_count, current_member_count, enable_read_receipt, create_time) " +
            "VALUES (1, 1, 2, 0, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "convId")
    int createSingleConversation(Conversation conversation);

    /**
     * 创建群聊会话
     */
    @Insert("INSERT INTO conversation (conv_type, conv_name, conv_owner_id, conv_status, max_member_count, current_member_count, enable_read_receipt, create_time) " +
            "VALUES (2, #{convName}, #{ownerId}, 1, 500, 0, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "convId")
    int createGroupConversation(@Param("convName") String convName, @Param("ownerId") Long ownerId);

    /**
     * 增加成员计数
     */
    @Update("UPDATE conversation SET current_member_count = current_member_count + 1 WHERE conv_id = #{convId}")
    int incrementMemberCount(@Param("convId") Long convId);

    /**
     * 减少成员计数
     */
    @Update("UPDATE conversation SET current_member_count = current_member_count - 1 WHERE conv_id = #{convId}")
    int decrementMemberCount(@Param("convId") Long convId);
}