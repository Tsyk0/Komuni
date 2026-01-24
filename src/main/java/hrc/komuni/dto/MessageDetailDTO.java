package hrc.komuni.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 优化后的消息详情DTO - 只包含必要的用户头像
 */
@Data
public class MessageDetailDTO {
    // 消息基本信息
    private Long messageId;
    private Long convId;
    private Long senderId;
    private String messageType;
    private String messageContent;
    private Integer messageStatus;
    private Boolean isRecalled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    // 移除：会话消息序列号
    // private Long convMsgSeq;

    // 发送者头像（直接字段，不需要单独类）
    private String senderAvatar;

    // 显示名称信息
    private String displayName;      // 最终显示名称
    private String memberNickname;   // 群昵称
    private String privateDisplayName; // 私有显示名称

    // 会话类型
    private Integer convType;        // 会话类型：1-单聊，2-群聊

    // 是否是自己发送的消息
    private Boolean isSentByMe;

    public MessageDetailDTO() {}
}