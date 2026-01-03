// FileAttachmentService.java
package hrc.komuni.service;

import hrc.komuni.entity.FileAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileAttachmentService {
    FileAttachment saveFile(MultipartFile file, Long convId, Long senderId) throws IOException;
    String getFilePath(Long fileId);
    FileAttachment getFileById(Long fileId);

    int insertFileAttachment(FileAttachment attachment);
}
