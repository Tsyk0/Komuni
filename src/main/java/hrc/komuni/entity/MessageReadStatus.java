package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class MessageReadStatus {
    private Long id;
    private Long messageId;
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date readTime;
}