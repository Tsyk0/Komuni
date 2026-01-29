package hrc.komuni.controller;

import hrc.komuni.dto.MessageDetailDTO;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.MessageDetailService;
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
@RequestMapping("/messageDetail")
@Tag(name = "消息详情管理", description = "消息详情相关的复合查询接口")
public class MessageDetailController {

    @Autowired
    private MessageDetailService messageDetailService;

    @Autowired
    private MessageService messageService;

    @GetMapping("/getMessageDetailsByConvId")
    @Operation(summary = "获取消息详情列表", description = "分页获取消息详情，包含发送者信息和显示名称")
    public ApiResponse<Map<String, Object>> getMessageDetailsByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "当前用户ID", required = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(description = "页码", required = false, example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", required = false, example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<MessageDetailDTO> messageDetails = messageDetailService.getMessageDetailsByConvId(
                    convId, page, pageSize, currentUserId // 传入currentUserId
            );
            Long total = messageService.countMessagesByConvId(convId);

            Map<String, Object> data = new HashMap<>();
            data.put("messages", messageDetails);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            return ApiResponse.serverError("查询消息详情失败: " + e.getMessage());
        }
    }
}