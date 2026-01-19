// src/main/java/hrc/komuni/util/ImageBase64Util.java
package hrc.komuni.util;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageBase64Util {

    /**
     * 将base64图片保存为文件
     * @param base64Data base64图片数据（完整格式：data:image/jpeg;base64,/9j/4AA...）
     * @param userId 用户ID
     * @return 文件相对路径，如 "/uploads/avatars/1_123456789.jpg"
     */
    public String saveBase64Image(String base64Data, Long userId) throws IOException {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }

        // 1. 检查是否是base64格式
        if (!base64Data.startsWith("data:image/")) {
            // 如果不是base64，可能是已有的URL路径，直接返回
            return base64Data;
        }

        System.out.println("开始处理base64图片，用户ID: " + userId);
        System.out.println("base64数据前100字符: " + base64Data.substring(0, Math.min(100, base64Data.length())));

        try {
            // 2. 提取base64数据部分
            String[] parts = base64Data.split(",");
            if (parts.length < 2) {
                throw new RuntimeException("base64格式错误，缺少数据部分");
            }

            String imageData = parts[1];
            String mimeType = parts[0].split(":")[1].split(";")[0]; // image/jpeg

            // 3. 获取文件扩展名
            String fileExtension = ".jpg"; // 默认
            if (mimeType.contains("png")) {
                fileExtension = ".png";
            } else if (mimeType.contains("gif")) {
                fileExtension = ".gif";
            } else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                fileExtension = ".jpg";
            }

            // 4. 解码base64
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            System.out.println("解码成功，图片大小: " + imageBytes.length + " bytes");

            // 5. 创建上传目录（与FileUploadUtil使用相同目录）
            String uploadDir = "./uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("创建目录: " + uploadDir);
            }

            // 6. 生成唯一文件名
            String fileName = userId + "_" + System.currentTimeMillis() + fileExtension;
            String filePath = uploadDir + fileName;

            // 7. 保存文件
            Files.write(Paths.get(filePath), imageBytes);
            System.out.println("文件保存成功: " + filePath);

            // 8. 返回相对路径
            return "/uploads/avatars/" + fileName;

        } catch (Exception e) {
            System.err.println("处理base64图片失败: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("处理图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断是否是base64图片
     */
    public boolean isBase64Image(String data) {
        return data != null && data.startsWith("data:image/");
    }

    /**
     * 删除旧头像文件
     */
    public void deleteOldAvatar(String oldAvatarPath) {
        if (oldAvatarPath == null || oldAvatarPath.isEmpty()) {
            return;
        }

        try {
            // 只删除相对路径的文件，不删除base64或完整URL
            if (!oldAvatarPath.startsWith("http") && !oldAvatarPath.startsWith("data:image/")) {
                String path = oldAvatarPath.startsWith("/") ?
                        "." + oldAvatarPath : oldAvatarPath;
                Files.deleteIfExists(Paths.get(path));
                System.out.println("删除旧头像成功: " + oldAvatarPath);
            }
        } catch (IOException e) {
            System.err.println("删除旧头像失败: " + oldAvatarPath + ", 错误: " + e.getMessage());
        }
    }
}