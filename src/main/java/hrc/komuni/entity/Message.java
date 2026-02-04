package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class Message {
    private Long messageId;

    @NotNull(message = "会话ID不能为空")
    private Long convId;

    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    @NotBlank(message = "消息类型不能为空")
    private String messageType; // text/image/file/audio/video/location/emoji/system

    @NotBlank(message = "消息内容不能为空")
    private String messageContent; // 文本内容或JSON格式的富内容

    private Integer messageStatus; // 0-发送中，1-已发送，2-已送达，3-已读，4-发送失败
    private Boolean isRecalled; // 是否撤回
    private Long replyToMessageId; // 回复的消息ID
    private List<Long> atUserIds; // @的用户ID列表（JSON序列化）

    // 移除convMsgSeq字段
    // private Long convMsgSeq; // 会话内消息序列号（从1开始）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recallTime;

    // 业务验证方法
    public void validate() {
        if (convId == null || convId <= 0) {
            throw new IllegalArgumentException("无效的会话ID");
        }
        if (senderId == null || senderId <= 0) {
            throw new IllegalArgumentException("无效的发送者ID");
        }
        if (messageType == null || messageType.trim().isEmpty()) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}