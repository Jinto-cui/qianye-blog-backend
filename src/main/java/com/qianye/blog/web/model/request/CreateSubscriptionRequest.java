package com.qianye.blog.web.model.request;

import lombok.Data;

/**
 * 提交简报订阅请求
 *
 * @author: Jinto Cui
 * @desc: 只需邮箱，确认 token 由后端生成并通过确认邮件发送。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Data
public class CreateSubscriptionRequest {

    /** 订阅邮箱 */
    private String email;
}
