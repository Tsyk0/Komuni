package hrc.komuni.controller;

import hrc.komuni.entity.Message;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/message")
@Tag(name = "消息管理", description = "消息的增删改查操作接口")
public class MessageController {
    @Autowired
    MessageService messageService;

    @GetMapping("/selectByMessageId")
    @Operation(summary = "查询消息详情", description = "根据消息ID查询消息的详细信息")
    public ApiResponse<Message> selectByMessageId(
            @Parameter(description = "消息ID", required = true) @RequestParam Long messageId) {
        try {
            Message message = messageService.selectMessageByMessageId(messageId);
            if (message == null) {
                return ApiResponse.notFound("消息不存在");
            }
            return ApiResponse.success("查询成功", message);
        } catch (Exception e) {
            return ApiResponse.serverError("查询消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/saveMessage")
    @Operation(summary = "保存消息", description = "保存一条新的消息记录")
    public ApiResponse<Message> saveMessage(
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

    @GetMapping("/getMessagesByConvId")
    @Operation(summary = "查询会话消息历史", description = "分页查询指定会话的消息历史记录")
    public ApiResponse<Map<String, Object>> getMessagesByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "页码", required = false, example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", required = false, example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<Message> messages = messageService.getMessagesByConvId(convId, page, pageSize);
            Long total = messageService.countMessagesByConvId(convId);

            Map<String, Object> data = new HashMap<>();
            data.put("messages", messages);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            return ApiResponse.serverError("查询消息历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/getLastMessage")
    @Operation(summary = "查询最后一条消息", description = "查询指定会话的最后一条消息")
    public ApiResponse<Message> getLastMessage(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Message message = messageService.getLastMessageByConvId(convId);
            return ApiResponse.success("查询成功", message);
        } catch (Exception e) {
            return ApiResponse.serverError("查询最后一条消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/recallMessage")
    @Operation(summary = "撤回消息", description = "撤回指定的消息，只有发送者可以撤回")
    public ApiResponse<String> recallMessage(
            @Parameter(description = "消息ID", required = true) @RequestParam Long messageId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = messageService.recallMessage(messageId, userId);
            if (result > 0) {
                return ApiResponse.success("撤回消息成功");
            } else {
                return ApiResponse.badRequest("撤回消息失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("撤回消息失败: " + e.getMessage());
        }
    }
}