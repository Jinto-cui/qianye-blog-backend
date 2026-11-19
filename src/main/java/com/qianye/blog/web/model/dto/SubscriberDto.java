package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订阅者响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 后台订阅者列表展示字段，不返回确认 token。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Data
public class SubscriberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订阅者 ID */
    private Long id;

    /** 订阅邮箱 */
    private String email;

    /** 是否已确认：0 = 待确认，1 = 已确认 */
    private Integer confirmed;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date subscribedAt;

    /** 退订时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date unsubscribedAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
