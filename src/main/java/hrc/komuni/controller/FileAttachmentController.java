package hrc.komuni.controller;

import hrc.komuni.entity.FileAttachment;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FileAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "文件上传、下载和管理相关的操作接口")
public class FileAttachmentController {
    @Autowired
    FileAttachmentService fileAttachmentService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件并关联到指定的会话和发送者")
    public ApiResponse<Map<String, Object>> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "会话ID", required = true) @RequestParam("convId") Long convId,
            @Parameter(description = "发送者ID", required = true) @RequestParam("senderId") Long senderId) {
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
    @Operation(summary = "获取文件下载路径", description = "根据文件ID获取文件的存储路径用于下载")
    public ApiResponse<String> downloadFile(
            @Parameter(description = "文件ID", required = true) @PathVariable Long fileId) {
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