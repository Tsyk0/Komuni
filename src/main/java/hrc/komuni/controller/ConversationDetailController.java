package hrc.komuni.controller;

import hrc.komuni.dto.ConversationDetailDTO;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/conversationDetail")
@Tag(name = "会话详情管理", description = "会话详情相关的复合查询接口")
public class ConversationDetailController {

    @Autowired
    private ConversationDetailService conversationDetailService;

    @GetMapping("/getConversationDetailsByUserId")
    @Operation(summary = "获取用户会话详情列表", description = "获取用户的会话列表，包含会话信息和最后一条消息")
    public ApiResponse<List<ConversationDetailDTO>> getConversationDetailsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestAttribute("userId") Long userId) {
        try {
            List<ConversationDetailDTO> details = conversationDetailService.getConversationDetailsByUserId(userId);
            return ApiResponse.success("查询成功", details);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话详情失败: " + e.getMessage());
        }
    }
}