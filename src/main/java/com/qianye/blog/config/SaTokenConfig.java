package com.qianye.blog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Sa-Token 权限框架配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/rest/v1/admin/**", r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }

    /**
     * 自定义 Token 生成策略：全小写 64 位 hex
     */
    @PostConstruct
    public void setTokenGenerateStrategy() {
        SaStrategy.instance.createToken = (loginId, loginType) -> {
            return UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");
        };
    }
}
