package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Integer userGender; // 0-未知，1-男，2-女
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date userBirthday;
    private String userLocation;
    private String userSignature;
    private String userPhone;
    private String userEmail;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userPassword;

    private Integer userStatus; // 0-封禁，1-正常
    private Integer onlineStatus; // 0-离线，1-在线

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}