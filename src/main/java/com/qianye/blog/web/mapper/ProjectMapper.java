package com.qianye.blog.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.blog.web.model.entity.Project;

/**
 * 项目展示 Mapper
 *
 * @author: Jinto Cui
 * @desc: 项目独立表的数据访问，替代旧 site_setting.projects JSON 嵌套
 * @date: 2026/05/19
 * @version: v1.0
 */
public interface ProjectMapper extends BaseMapper<Project> {
}
