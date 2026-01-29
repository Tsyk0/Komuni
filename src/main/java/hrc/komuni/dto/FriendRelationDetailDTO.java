package hrc.komuni.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendRelationDetailDTO {
    // FriendRelation字段
    private Long id;
    private Long userId;
    private Long friendId;
    private Integer relationStatus;
    private String remarkName;
    private String friendGroup;
    private String addSource;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;

    // User字段（friendId对应的用户信息）
    private String friendNickname;
    private String friendAvatar;
    private Integer friendGender;
    private String friendSignature;
    private Integer friendOnlineStatus;
}