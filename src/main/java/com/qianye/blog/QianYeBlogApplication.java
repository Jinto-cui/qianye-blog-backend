package com.qianye.blog;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.qianye.blog.web.mapper")
@SpringBootApplication
@Slf4j
public class QianYeBlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(QianYeBlogApplication.class, args);
		log.info("博客项目后端服务启动成功啦！！！");
	}

}
