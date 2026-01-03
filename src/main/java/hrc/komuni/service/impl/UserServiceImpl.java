package hrc.komuni.service.impl;

import hrc.komuni.entity.User;
import hrc.komuni.mapper.UserMapper;
import hrc.komuni.service.UserService;
import hrc.komuni.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public User selectUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

    @Override
    public Long insertUser(User user) {
        String originalPwd = user.getUserPassword();
        if (originalPwd == null || originalPwd.trim().isEmpty()) {
            return 0L;
        }
        if (user.getUserNickname() == null || user.getUserNickname().trim().isEmpty()) {
            return 0L;
        }

        // 清除传入的 userId，让数据库自动生成
        user.setUserId(null);

        user.setUserPassword(encryptPwd(originalPwd));
        user.setUserStatus(1); // 默认正常状态
        user.setOnlineStatus(0); // 默认离线
        user.setCreateTime(new Date());

        try {
            int res = userMapper.insertUser(user);
            if (res == 1) {
                return user.getUserId();
            }
            return 0L;
        } catch (Exception e) {
            // 记录异常信息以便调试
            e.printStackTrace();
            throw new RuntimeException("插入用户失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String encryptPwd(String originalPwd) {
        return PasswordUtil.encryptPassword(originalPwd);
    }

    @Override
    public Boolean validatePwd(String inputPwd, String storedPwd) {
        return BCrypt.checkpw(inputPwd, storedPwd);
    }

    @Override
    public boolean checkUserPwd(Long userId, String inputPwd) {
        String storedPwd = getUserPwdByUserId(userId);
        if (storedPwd == null) {
            return false;
        }
        return validatePwd(inputPwd, storedPwd);
    }

    @Override
    public String getUserPwdByUserId(Long userId) {
        return userMapper.getUserPwdByUserId(userId);
    }

    @Override
    public String updateUserPwdByUserId(Long userId, String newPwd) {
        String cryptedPwd = PasswordUtil.encryptPassword(newPwd);
        return userMapper.updateUserPwdByUserId(userId, cryptedPwd) == 1 ? "更新成功" : "更新失败";
    }

    @Override
    public String updateUserAllAttriByUserId(User user) {
        User storedUser = selectUserByUserId(user.getUserId());
        if (storedUser == null) {
            return "用户不存在";
        }

        if (user.getUserPassword() != null && !checkUserPwd(user.getUserId(), user.getUserPassword())) {
            String cryptedPwd = PasswordUtil.encryptPassword(user.getUserPassword());
            user.setUserPassword(cryptedPwd);
        }

        int updateResult = userMapper.updateUserAllAttriByUserId(user);
        return updateResult == 1 ? "更新成功" : "更新失败";
    }

    @Override
    public List<User> selectUsersCondition(String key, Integer amount) {
        return userMapper.selectUsersCondition(key, amount);
    }

    @Override
    public int updateOnlineStatus(Long userId, Integer status) {
        return userMapper.updateOnlineStatus(userId, status);
    }
}