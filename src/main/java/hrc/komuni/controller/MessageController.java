package hrc.komuni.controller;

import hrc.komuni.entity.Message;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    MessageService messageService;

    @GetMapping("/selectByMessageId")
    public ApiResponse<Message> selectByMessageId(@RequestParam Long messageId) {
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
    public ApiResponse<Message> saveMessage(@RequestBody Message message) {
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
    public ApiResponse<Map<String, Object>> getMessagesByConvId(
            @RequestParam Long convId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
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
    public ApiResponse<Message> getLastMessage(@RequestParam Long convId) {
        try {
            Message message = messageService.getLastMessageByConvId(convId);
            return ApiResponse.success("查询成功", message);
        } catch (Exception e) {
            return ApiResponse.serverError("查询最后一条消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/recallMessage")
    public ApiResponse<String> recallMessage(
            @RequestParam Long messageId,
            @RequestParam Long userId) {
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