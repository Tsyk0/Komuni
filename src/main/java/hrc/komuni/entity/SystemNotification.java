package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class SystemNotification {
    private Long notificationId;
    private Long receiverId;
    private String notificationType; // friend_request/group_invite/system_message
    private String notificationTitle;
    private String notificationContent;
    private Long relatedUserId;
    private Long relatedConvId;
    private Boolean isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date readTime;
}