package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Message {
    private Long messageId;
    private Long convId;
    private Long senderId;
    private Long receiverId; // 单聊时使用
    private String messageType; // text/image/file/audio/video/location/emoji/system
    private String messageContent; // 文本内容或JSON格式的富内容
    private Integer messageStatus; // 0-发送中，1-已发送，2-已送达，3-已读，4-发送失败
    private Boolean isRecalled; // 是否撤回
    private Long replyToMessageId; // 回复的消息ID
    private List<Long> atUserIds; // @的用户ID列表（JSON序列化）

    // 新增字段：会话内消息序列号
    private Long convMsgSeq; // 会话内消息序列号（从1开始）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recallTime;

}