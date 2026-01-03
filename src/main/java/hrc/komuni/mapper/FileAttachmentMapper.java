// FileAttachmentMapper.java
package hrc.komuni.mapper;

import hrc.komuni.entity.FileAttachment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FileAttachmentMapper {
    @Insert("INSERT INTO file_attachment (" +
            "message_id, uploader_id, file_name, file_type, file_size, file_path, thumbnail_path, file_md5, upload_time" +
            ") VALUES (" +
            "#{messageId}, #{uploaderId}, #{fileName}, #{fileType}, #{fileSize}, #{filePath}, #{thumbnailPath}, #{fileMd5}, #{uploadTime}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    int insertFileAttachment(FileAttachment attachment);

    @Select("SELECT * FROM file_attachment WHERE file_id = #{fileId}")
    FileAttachment selectFileAttachmentById(@Param("fileId") Long fileId);

    @Select("SELECT * FROM file_attachment WHERE message_id = #{messageId}")
    FileAttachment selectFileAttachmentByMessageId(@Param("messageId") Long messageId);

    @Update("UPDATE file_attachment SET message_id = #{messageId} WHERE file_id = #{fileId}")
    int updateMessageId(@Param("fileId") Long fileId, @Param("messageId") Long messageId);
}
