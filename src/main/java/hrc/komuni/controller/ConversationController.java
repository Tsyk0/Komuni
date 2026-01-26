package hrc.komuni.controller;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationService;
import hrc.komuni.service.ConversationMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/conversation")
@Tag(name = "会话管理", description = "会话相关的操作接口")
public class ConversationController {
    @Autowired
    ConversationService conversationService;

    @Autowired
    ConversationMemberService conversationMemberService;

    @GetMapping("/selectConversationByConvId")
    @Operation(summary = "查询会话信息", description = "根据会话ID查询会话详细信息")
    public ApiResponse<Conversation> selectConversationByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Conversation conv = conversationService.selectConversationByConvId(convId);
            if (conv == null) {
                return ApiResponse.notFound("会话不存在");
            }
            return ApiResponse.success("查询成功", conv);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话失败: " + e.getMessage());
        }
    }



    @GetMapping("/selectConvIdsByUserId")
    @Operation(summary = "查询用户参与的会话ID", description = "根据用户ID查询该用户参与的所有会话ID列表")
    public ApiResponse<List<Long>> selectConvIdsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Long> convIds = conversationMemberService.selectConvIdsByUserId(userId);
            return ApiResponse.success("查询成功", convIds);
        } catch (Exception e) {
            return ApiResponse.serverError("查询用户会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/getConversationsByUserId")
    @Operation(summary = "查询用户的会话列表", description = "根据用户ID查询该用户参与的所有会话详情列表")
    public ApiResponse<List<Conversation>> getConversationsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
            return ApiResponse.success("查询成功", conversations);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话列表失败: " + e.getMessage());
        }
    }





    @GetMapping("/selectMembersByConvId")
    @Operation(summary = "查询会话成员列表", description = "根据会话ID查询该会话的所有成员信息")
    public ApiResponse<List<ConversationMember>> selectMembersByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询成员失败: " + e.getMessage());
        }
    }


}