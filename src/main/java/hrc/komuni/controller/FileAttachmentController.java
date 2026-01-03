// FileAttachmentController.java
package hrc.komuni.controller;

import hrc.komuni.entity.FileAttachment;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/file")
public class FileAttachmentController {
    @Autowired
    FileAttachmentService fileAttachmentService;

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("convId") Long convId,
            @RequestParam("senderId") Long senderId) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.badRequest("文件不能为空");
            }

            FileAttachment attachment = fileAttachmentService.saveFile(file, convId, senderId);
            if (attachment == null) {
                return ApiResponse.serverError("文件上传失败");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("attachment", attachment);
            data.put("messageType", "file");
            data.put("messageContent", attachment.getFileName());

            return ApiResponse.success("文件上传成功", data);
        } catch (IOException e) {
            return ApiResponse.serverError("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.serverError("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ApiResponse<String> downloadFile(@PathVariable Long fileId) {
        try {
            String filePath = fileAttachmentService.getFilePath(fileId);
            if (filePath == null) {
                return ApiResponse.notFound("文件不存在");
            }
            return ApiResponse.success("获取文件路径成功", filePath);
        } catch (Exception e) {
            return ApiResponse.serverError("获取文件失败: " + e.getMessage());
        }
    }
}
