package hrc.komuni.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 好友详情DTO：包含好友关系信息 + 好友用户信息（排除密码、创建/更新时间）
 */
@Data
public class FriendInfoDTO {
    // ========== friend_relation 表字段 ==========
    private Long id;
    private Long userId;
    private Long friendId;
    private Integer relationStatus;
    private String remarkName;
    private String friendGroup;
    private String addSource;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;

    // ========== user 表字段（排除 password, create_time, update_time） ==========
    private String friendNickname;
    private String friendAvatar;
    private Integer friendGender;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date friendBirthday;
    private String friendLocation;
    private String friendSignature;
    private String friendPhone;
    private String friendEmail;
    private Integer friendStatus;
    private Integer friendOnlineStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date friendLastLoginTime;
}
