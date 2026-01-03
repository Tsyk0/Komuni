package hrc.komuni.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class FriendRelation {
    private Long id;
    private Long userId;
    private Long friendId;
    private Integer relationStatus; // 0-已删除，1-正常，2-拉黑
    private String remarkName;
    private String friendGroup;
    private String addSource;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}