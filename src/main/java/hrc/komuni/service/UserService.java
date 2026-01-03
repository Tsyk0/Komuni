package hrc.komuni.service;

import hrc.komuni.entity.User;
import java.util.List;

public interface UserService {
    User selectUserByUserId(Long userId);

    Long insertUser(User user);

    String encryptPwd(String originalPwd);

    Boolean validatePwd(String storedPwd, String inputPwd);

    boolean checkUserPwd(Long userId, String inputPwd);

    String getUserPwdByUserId(Long userId);

    String updateUserPwdByUserId(Long userId, String newPwd);

    String updateUserAllAttriByUserId(User user);

    List<User> selectUsersCondition(String key, Integer amount);

    int updateOnlineStatus(Long userId, Integer status);
}