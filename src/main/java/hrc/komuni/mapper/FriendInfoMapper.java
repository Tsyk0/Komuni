package hrc.komuni.mapper;

import hrc.komuni.dto.FriendInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FriendInfoMapper {

    @Select("SELECT " +
            "fr.id, fr.user_id, fr.friend_id, fr.relation_status, fr.remark_name, " +
            "fr.friend_group, fr.add_source, fr.add_time, fr.update_time, " +
            "u.user_nickname AS friendNickname, u.user_avatar AS friendAvatar, " +
            "u.user_gender AS friendGender, u.user_birthday AS friendBirthday, " +
            "u.user_location AS friendLocation, u.user_signature AS friendSignature, " +
            "u.user_phone AS friendPhone, u.user_email AS friendEmail, " +
            "u.user_status AS friendStatus, u.online_status AS friendOnlineStatus, " +
            "u.last_login_time AS friendLastLoginTime " +
            "FROM friend_relation fr " +
            "JOIN user u ON fr.friend_id = u.user_id " +
            "WHERE fr.user_id = #{userId} AND fr.friend_id = #{friendId}")
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
            @Result(property = "friendNickname", column = "friendNickname"),
            @Result(property = "friendAvatar", column = "friendAvatar"),
            @Result(property = "friendGender", column = "friendGender"),
            @Result(property = "friendBirthday", column = "friendBirthday"),
            @Result(property = "friendLocation", column = "friendLocation"),
            @Result(property = "friendSignature", column = "friendSignature"),
            @Result(property = "friendPhone", column = "friendPhone"),
            @Result(property = "friendEmail", column = "friendEmail"),
            @Result(property = "friendStatus", column = "friendStatus"),
            @Result(property = "friendOnlineStatus", column = "friendOnlineStatus"),
            @Result(property = "friendLastLoginTime", column = "friendLastLoginTime")
    })
    FriendInfoDTO getFriendInfoByUserIdAndFriendId(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId);
}
