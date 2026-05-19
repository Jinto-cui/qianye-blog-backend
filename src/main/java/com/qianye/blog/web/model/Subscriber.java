package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简报订阅者
 *
 * @author: Jinto Cui
 * @desc: 独立订阅表（非用户表）。旧 bg_subscribed_user 实际是旧版用户表，已被 user 表替代。
 *        本表专注于 Newsletter 邮件订阅，与 user 表解耦。
 * @date: 2026/05/20
 * @version: v1.0
 * @table: subscriber
 */
@TableName(value = "subscriber")
@Data
public class Subscriber implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 订阅邮箱 */
    private String email;

    /** 确认 token（UUID，用于确认邮件链接） */
    private String token;

    /** 是否已确认：0 = 待确认，1 = 已确认 */
    private Integer confirmed;

    /** 确认时间（即正式订阅时间） */
    private Date subscribedAt;

    /** 取消订阅时间（NULL 表示仍在订阅中） */
    private Date unsubscribedAt;

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
