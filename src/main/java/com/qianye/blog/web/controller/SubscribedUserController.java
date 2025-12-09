package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.SubscribedUser;
import com.qianye.blog.web.model.request.SubscribedUserPageRequest;
import com.qianye.blog.web.service.SubscribedUserService;
import com.qianye.blog.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅用户接口
 * 提供订阅用户的增删改查与分页查询
 */
@RestController
@RequestMapping("/subscribed-user")
public class SubscribedUserController {
    @Autowired
    private SubscribedUserService subscribedUserService;

    /**
     * 新增订阅用户
     * @param entity 订阅用户实体
     * @return 主键ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody SubscribedUser entity) {
        subscribedUserService.save(entity);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 根据ID获取订阅用户
     * @param id 主键ID
     * @return 订阅用户详情
     */
    @GetMapping("/get")
    public Result<SubscribedUser> get(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(subscribedUserService.getById(id));
    }

    /**
     * 列出所有订阅用户
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<SubscribedUser>> list() {
        return ResultUtils.success(subscribedUserService.list());
    }

    /**
     * 分页查询订阅用户
     * 支持按用户名、昵称、邮箱、状态筛选
     * @param req 分页与筛选参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<Page<SubscribedUser>> page(@RequestBody SubscribedUserPageRequest req) {
        int pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        Page<SubscribedUser> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SubscribedUser> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(req.getUserName())) {
            qw.like("user_name", req.getUserName());
        }
        if (StringUtils.isNotBlank(req.getNickName())) {
            qw.like("nick_name", req.getNickName());
        }
        if (StringUtils.isNotBlank(req.getEmail())) {
            qw.like("email", req.getEmail());
        }
        if (StringUtils.isNotBlank(req.getStatus())) {
            qw.eq("status", req.getStatus());
        }
        return ResultUtils.success(subscribedUserService.page(page, qw));
    }

    /**
     * 更新订阅用户
     * @param entity 订阅用户实体（需包含主键）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody SubscribedUser entity) {
        if (entity.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(subscribedUserService.updateById(entity));
    }

    /**
     * 删除订阅用户（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(subscribedUserService.removeById(id));
    }
}