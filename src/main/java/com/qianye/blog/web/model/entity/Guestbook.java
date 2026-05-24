package com.qianye.blog.web.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 留言墙
 *
 * @author: Jinto Cui
 * @desc: user_id 关联 user 表 JOIN 获取头像昵称，不再冗余存 user_info JSON。
 *        body 替代旧 message 字段，与 comment 表命名一致。
 * @date: 2026/05/20
 * @version: v2.0
 * @table: guestbook
 */
@TableName(value = "guestbook")
@Data
public class Guestbook implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 留言用户 ID（关联 user.id） */
    private Long userId;

    /** 留言内容 */
    private String body;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 逻辑删除：0 = 未删除，1 = 已删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
