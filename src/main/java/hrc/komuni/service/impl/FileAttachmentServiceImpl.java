// FileAttachmentServiceImpl.java
package hrc.komuni.service.impl;

import hrc.komuni.entity.FileAttachment;
import hrc.komuni.mapper.FileAttachmentMapper;
import hrc.komuni.service.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class FileAttachmentServiceImpl implements FileAttachmentService {
    @Autowired
    FileAttachmentMapper fileAttachmentMapper;

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public FileAttachment saveFile(MultipartFile file, Long convId, Long senderId) throws IOException {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String fileName = System.currentTimeMillis() + "_" +
                senderId + "_" + convId + extension;

        // 保存文件
        File destFile = new File(uploadDir, fileName);
        file.transferTo(destFile);

        // 创建附件记录
        FileAttachment attachment = new FileAttachment();
        attachment.setUploaderId(senderId);
        attachment.setFileName(originalFilename);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(uploadPath + fileName);
        attachment.setUploadTime(new Date());

        // 保存到数据库
        int result = fileAttachmentMapper.insertFileAttachment(attachment);
        if (result > 0) {
            return attachment;
        }
        return null;
    }

    @Override
    public String getFilePath(Long fileId) {
        FileAttachment attachment = fileAttachmentMapper.selectFileAttachmentById(fileId);
        return attachment != null ? attachment.getFilePath() : null;
    }

    @Override
    public FileAttachment getFileById(Long fileId) {
        return fileAttachmentMapper.selectFileAttachmentById(fileId);
    }

    @Override
    public int insertFileAttachment(FileAttachment attachment) {
        return fileAttachmentMapper.insertFileAttachment(attachment);
    }
}
