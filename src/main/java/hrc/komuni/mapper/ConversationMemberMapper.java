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

    @Select("SELECT DISTINCT conv_id FROM conversation_member WHERE user_id = #{userId} AND member_status = 1")
    List<Long> selectConvIdsByUserId(@Param("userId") Long userId);

    @Update("UPDATE conversation_member SET " +
            "last_read_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE conv_id = #{convId} AND user_id = #{userId}")
    int updateLastReadTime(@Param("convId") Long convId, @Param("userId") Long userId);



}