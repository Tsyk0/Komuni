package hrc.komuni.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// FileUploadUtil.java
@Component
public class FileUploadUtil {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.avatar-path}")
    private String avatarPath;

    /**
     * 上传头像文件
     * @param file 图片文件
     * @param userId 用户ID
     * @return 相对路径，如 "/uploads/avatars/1_123456789.jpg"
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 1. 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }

        // 2. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = userId + "_" + System.currentTimeMillis() + fileExtension;

        // 3. 创建目录
        Path avatarDir = Paths.get(uploadPath + avatarPath);
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }

        // 4. 保存文件
        Path filePath = avatarDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 5. 返回相对路径（供前端访问）
        return "/uploads" + avatarPath + fileName;
    }

    /**
     * 删除旧头像文件
     * @param oldAvatarPath 旧头像路径
     */
    public void deleteOldAvatar(String oldAvatarPath) {
        if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
            try {
                Path oldPath = Paths.get("." + oldAvatarPath); // 去掉开头的"/"
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                // 删除失败只记录日志，不影响主流程
                System.err.println("删除旧头像失败: " + oldAvatarPath);
            }
        }
    }
}