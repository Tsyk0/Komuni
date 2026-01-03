package hrc.komuni.mapper;

import hrc.komuni.entity.User;

public class UserMapperProvider {

    public static String updateUserAllAttriByUserId(User user) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE user SET ");

        if (user.getUserNickname() != null) {
            sql.append("user_nickname = #{userNickname}, ");
        }
        if (user.getUserAvatar() != null) {
            sql.append("user_avatar = #{userAvatar}, ");
        }
        if (user.getUserGender() != null) {
            sql.append("user_gender = #{userGender}, ");
        }
        if (user.getUserBirthday() != null) {
            sql.append("user_birthday = #{userBirthday}, ");
        }
        if (user.getUserLocation() != null) {
            sql.append("user_location = #{userLocation}, ");
        }
        if (user.getUserSignature() != null) {
            sql.append("user_signature = #{userSignature}, ");
        }
        if (user.getUserPhone() != null) {
            sql.append("user_phone = #{userPhone}, ");
        }
        if (user.getUserEmail() != null) {
            sql.append("user_email = #{userEmail}, ");
        }
        if (user.getUserPassword() != null) {
            sql.append("user_password = #{userPassword}, ");
        }
        if (user.getUserStatus() != null) {
            sql.append("user_status = #{userStatus}, ");
        }
        if (user.getOnlineStatus() != null) {
            sql.append("online_status = #{onlineStatus}, ");
        }

        sql.append("update_time = NOW() WHERE user_id = #{userId}");

        return sql.toString();
    }
}