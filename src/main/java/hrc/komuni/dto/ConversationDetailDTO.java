package hrc.komuni.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 会话详情DTO - 包含会话信息、最后一条消息和显示信息
 */
@Data
public class ConversationDetailDTO {
    // 会话基本信息
    private Long convId;
    private Integer convType;           // 1-单聊，2-群聊
    private String convName;           // 会话公共名称（群聊时使用）
    private String convAvatar;         // 会话头像

    // 会话状态信息
    private Integer currentMemberCount;
    private Integer maxMemberCount;
    private Integer convStatus;
    // 移除：private Long currentMsgSeq;        // 当前消息序列号

    // 会话成员相关
    private String privateDisplayName; // 用户设置的私有显示名称（单聊时使用）
    private Integer unreadCount;       // 未读消息数

    // 最后一条消息信息
    private LastMessageInfo lastMessage;

    // 时间信息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;           // 会话更新时间

    @Data
    public static class LastMessageInfo {
        private Long messageId;
        private Long senderId;
        private String messageType;
        private String messageContent;
        private String senderDisplayName;  // 发送者显示名称（优先群昵称）
        private String senderAvatar;       // 发送者头像

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date sendTime;
    }
}