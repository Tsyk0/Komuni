package hrc.komuni.mapper;

import hrc.komuni.entity.ConversationMember;
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
}