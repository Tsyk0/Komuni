package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class ConversationMember {
    private Long id;
    private Long convId;
    private Long userId;
    private String memberNickname; // 群内昵称
    private Integer memberRole; // 0-普通成员，1-管理员，2-群主
    private Integer memberStatus; // 0-已退出，1-正常，2-禁言
    private String privateDisplayName; // 单聊时的对方昵称

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastReadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastSpeakTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // 新增字段：未读消息数和最后阅读消息序列号
    private Integer unreadCount; // 未读消息数（缓存，提高性能）
    private Long lastReadMessageSeq; // 最后阅读的消息序列号（会话内）
}