package hrc.komuni.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密密码
    public static String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(rawPassword);
    }

    // 验证密码
    public static boolean matchPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
