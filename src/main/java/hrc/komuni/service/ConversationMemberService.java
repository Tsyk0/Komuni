package hrc.komuni.service;

import hrc.komuni.entity.ConversationMember;
import java.util.List;

public interface ConversationMemberService {

    // 1. 查询相关方法
    ConversationMember selectByConvIdAndUserId(Long convId, Long userId);
    List<ConversationMember> selectMembersByConvId(Long convId);
    List<ConversationMember> selectByUserId(Long userId);

    List<Long> selectConvIdsByUserId(Long userId);

    int updateLastReadTime(Long convId, Long userId);

    /**
     * 获取指定群聊的所有成员信息（压缩版）
     */
    List<hrc.komuni.dto.CompressedConvMemberDTO> getCompressedMembers(Long convId);



    // ============ 未在Controller中使用的方法（放在底端） ============
}