package hrc.komuni.mapper;

import hrc.komuni.entity.FriendRelation;
import hrc.komuni.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendRelationMapper {

    @Select("SELECT u.user_nickname FROM user u " +
            "JOIN friend_relation fr ON u.user_id = fr.friend_id " +
            "WHERE fr.user_id = #{userId} AND fr.relation_status = 1")
    List<String> selectAllFriendsNameByUserId(@Param("userId") Long userId);

    @Select("SELECT u.* FROM user u " +
            "JOIN friend_relation fr ON u.user_id = fr.friend_id " +
            "WHERE fr.user_id = #{userId} AND fr.relation_status = 1")
    List<User> selectAllFriendsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM friend_relation " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    FriendRelation selectByUserIdAndFriendId(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId);

    @Insert("INSERT INTO friend_relation (" +
            "user_id, friend_id, relation_status, remark_name, friend_group, add_source, add_time" +
            ") VALUES (" +
            "#{userId}, #{friendId}, #{relationStatus}, #{remarkName}, #{friendGroup}, #{addSource}, #{addTime}" +
            ")")
    int insertFriendRelation(FriendRelation friendRelation);

    @Update("UPDATE friend_relation SET " +
            "relation_status = 0, update_time = NOW() " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int removeFriendRelation(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId);

    @Update("UPDATE friend_relation SET " +
            "relation_status = #{status}, remark_name = #{remarkName}, " +
            "friend_group = #{friendGroup}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int updateFriendRelation(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            @Param("status") Integer status,
            @Param("remarkName") String remarkName,
            @Param("friendGroup") String friendGroup);
}