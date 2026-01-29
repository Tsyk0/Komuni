package hrc.komuni.mapper;

import hrc.komuni.dto.FriendRelationDetailDTO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FriendRelationDetailMapper {

    @Select("SELECT " +
            "fr.id, " +
            "fr.user_id, " +
            "fr.friend_id, " +
            "fr.relation_status, " +
            "fr.remark_name, " +
            "fr.friend_group, " +
            "fr.add_source, " +
            "fr.add_time, " +
            "fr.update_time, " +
            "u.user_nickname as friend_nickname, " +
            "u.user_avatar as friend_avatar, " +
            "u.user_gender as friend_gender, " +
            "u.user_signature as friend_signature, " +
            "u.online_status as friend_online_status " +
            "FROM friend_relation fr " +
            "LEFT JOIN user u ON fr.friend_id = u.user_id " +
            "WHERE fr.user_id = #{userId} " +
            "    AND fr.relation_status = 1 " +
            "ORDER BY fr.update_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "friendId", column = "friend_id"),
            @Result(property = "relationStatus", column = "relation_status"),
            @Result(property = "remarkName", column = "remark_name"),
            @Result(property = "friendGroup", column = "friend_group"),
            @Result(property = "addSource", column = "add_source"),
            @Result(property = "addTime", column = "add_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "friendNickname", column = "friend_nickname"),
            @Result(property = "friendAvatar", column = "friend_avatar"),
            @Result(property = "friendGender", column = "friend_gender"),
            @Result(property = "friendSignature", column = "friend_signature"),
            @Result(property = "friendOnlineStatus", column = "friend_online_status")
    })
    List<FriendRelationDetailDTO> getFriendListByUserId(@Param("userId") Long userId);
}