package hrc.komuni.service;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import java.util.List;

public interface ConversationService {
    Conversation selectConversationByConvId(Long convId);

    List<Long> selectConvIdsByUserId(Long userId);

    String getConvNameByConvId(Long convId);

    String getPrivateDisplayName(Long convId, Long userId);

    Integer getConvTypeByConvId(Long convId);

    Long createSingleConversation(Long user1Id, Long user2Id);

    Long createGroupConversation(Long ownerId, String convName);

    int addMemberToConversation(Long convId, Long userId, String memberNickname);

    int updatePrivateDisplayName(Long convId, Long userId, String displayName);

    Integer getMemberCount(Long convId);

    List<ConversationMember> getMembersByConvId(Long convId);

    List<Conversation> getConversationsByUserId(Long userId);

    int removeMember(Long convId, Long userId);

    int updateLastReadTime(Long convId, Long userId);

    // 新增：更新群聊的已读回执设置
    int updateReadReceiptSetting(Long convId, Boolean enableReadReceipt);

    // 新增：查询群聊的已读回执设置
    Boolean getReadReceiptSetting(Long convId);

//    int getUnreadMessageCount(Long convId, Long userId);
}