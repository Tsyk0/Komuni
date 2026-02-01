package hrc.komuni.controller;

import hrc.komuni.entity.Conversation;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/conversation")
@Tag(name = "会话管理", description = "会话信息更新等操作接口")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/updateConversationAttriUserOrientedByConvId")
    @Operation(summary = "更新会话信息（用户侧可修改字段）", description = "仅可更新 conv_type、conv_name、conv_avatar、conv_description、enable_read_receipt，不可修改 conv_status、conv_owner_id、人数等")
    public ApiResponse<String> updateConversationAttriUserOrientedByConvId(
            @Parameter(description = "会话信息（需包含 convId，其余为要更新的字段）", required = true) @RequestBody Conversation conversation) {
        try {
            String result = conversationService.updateConversationAttriUserOrientedByConvId(conversation);
            return "更新成功".equals(result)
                    ? ApiResponse.success(result)
                    : ApiResponse.badRequest(result);
        } catch (Exception e) {
            return ApiResponse.serverError("更新会话信息失败: " + e.getMessage());
        }
    }
}
