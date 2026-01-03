package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class Conversation {
    private Long convId;
    private Integer convType; // 1-单聊，2-群聊
    private String convName;
    private String convAvatar;
    private String convDescription;
    private Long convOwnerId;
    private Integer maxMemberCount;
    private Integer currentMemberCount;
    private Integer convStatus; // 0-已解散，1-正常
    private Boolean enableReadReceipt; // 是否启用消息已读回执（群聊设置）

    // 新增字段：当前消息序列号
    private Long currentMsgSeq; // 当前消息序列号（会话内）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}