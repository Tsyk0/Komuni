package hrc.komuni.controller;

import hrc.komuni.dto.CompressedConvMemberDTO;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/compressedCM")
@Tag(name = "群成员管理", description = "群成员查询与管理接口")
public class CompressedConvMemberController {

    @Autowired
    private ConversationMemberService conversationMemberService;

    @GetMapping("/getCompressedCM")
    @Operation(summary = "获取群聊成员列表", description = "获取指定群聊的所有成员信息，包含群内昵称、头像和角色")
    public ApiResponse<List<CompressedConvMemberDTO>> getGroupMembers(
            @Parameter(description = "群聊会话ID", required = true) @RequestParam Integer convId) {
        try {
            // 将 Integer 转换为 Long 以匹配 Service 层定义
            List<CompressedConvMemberDTO> members = conversationMemberService.getCompressedMembers(convId.longValue());
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询群成员失败: " + e.getMessage());
        }
    }
}
