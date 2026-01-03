package hrc.komuni.mapper;

import hrc.komuni.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User selectUserByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user (" +
            "user_nickname, user_avatar, user_gender, user_birthday, user_location, " +
            "user_signature, user_phone, user_email, user_password, user_status, " +
            "online_status, create_time" +
            ") VALUES (" +
            "#{userNickname}, #{userAvatar}, #{userGender}, #{userBirthday}, #{userLocation}, " +
            "#{userSignature}, #{userPhone}, #{userEmail}, #{userPassword}, #{userStatus}, " +
            "#{onlineStatus}, #{createTime}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insertUser(User user);

    @Select("SELECT user_password FROM user WHERE user_id = #{userId}")
    String getUserPwdByUserId(@Param("userId") Long userId);

    @Update("UPDATE user SET user_password = #{newPwd}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateUserPwdByUserId(@Param("userId") Long userId, @Param("newPwd") String newPwd);

    @UpdateProvider(type = UserMapperProvider.class, method = "updateUserAllAttriByUserId")
    int updateUserAllAttriByUserId(User user);

    @Select("SELECT * FROM user " +
            "WHERE (user_nickname LIKE CONCAT('%', #{key}, '%') OR user_id LIKE CONCAT('%', #{key}, '%')) " +
            "AND user_status = 1 " +
            "LIMIT #{amount}")
    List<User> selectUsersCondition(@Param("key") String key, @Param("amount") Integer amount);

    @Update("UPDATE user SET online_status = #{status}, last_login_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateOnlineStatus(@Param("userId") Long userId, @Param("status") Integer status);
}