package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class FileAttachment {
    private Long fileId;
    private Long messageId;
    private Long uploaderId;
    private String fileName;
    private String fileType; // MIME类型
    private Long fileSize; // 字节
    private String filePath;
    private String thumbnailPath;
    private String fileMd5;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date uploadTime;
}