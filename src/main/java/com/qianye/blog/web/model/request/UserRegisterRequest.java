package com.qianye.blog.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String nickname;

    private String email;
}
