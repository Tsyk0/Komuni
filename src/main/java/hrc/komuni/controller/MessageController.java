package hrc.komuni.controller;

import hrc.komuni.entity.Message;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/message")
@Tag(name = "消息管理", description = "消息的增删改查操作接口")
public class MessageController {
    @Autowired
    MessageService messageService;

    @PostMapping("/sendMessage")
    @Operation(summary = "发送消息", description = "发送一条新的消息记录")
    public ApiResponse<Message> sendMessage(
            @Parameter(description = "消息对象", required = true) @RequestBody Message message) {
        try {
            Long messageId = messageService.insertMessage(message);
            if (messageId != null) {
                return ApiResponse.success("消息保存成功", message);
            } else {
                return ApiResponse.badRequest("消息保存失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("保存消息失败: " + e.getMessage());
        }
    }
}