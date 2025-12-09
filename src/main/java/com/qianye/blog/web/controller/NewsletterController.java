package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Newsletter;
import com.qianye.blog.web.model.request.NewsletterPageRequest;
import com.qianye.blog.web.service.NewsletterService;
import com.qianye.blog.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 简报接口
 * 提供简报的增删改查与分页查询
 */
@RestController
@RequestMapping("/newsletter")
public class NewsletterController {
    @Autowired
    private NewsletterService newsletterService;

    /**
     * 新增简报
     * @param entity 简报实体
     * @return 主键ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody Newsletter entity) {
        newsletterService.save(entity);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 根据ID获取简报
     * @param id 主键ID
     * @return 简报详情
     */
    @GetMapping("/get")
    public Result<Newsletter> get(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(newsletterService.getById(id));
    }

    /**
     * 列出所有简报
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<Newsletter>> list() {
        return ResultUtils.success(newsletterService.list());
    }

    /**
     * 分页查询简报
     * 支持按标题与发送时间范围过滤
     * @param req 分页与筛选参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<Page<Newsletter>> page(@RequestBody NewsletterPageRequest req) {
        int pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        Page<Newsletter> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Newsletter> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(req.getSubject())) {
            qw.like("subject", req.getSubject());
        }
        if (req.getStartSentAt() != null) {
            qw.ge("sent_at", req.getStartSentAt());
        }
        if (req.getEndSentAt() != null) {
            qw.le("sent_at", req.getEndSentAt());
        }
        return ResultUtils.success(newsletterService.page(page, qw));
    }

    /**
     * 更新简报
     * @param entity 简报实体（需包含主键）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Newsletter entity) {
        if (entity.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(newsletterService.updateById(entity));
    }

    /**
     * 删除简报（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(newsletterService.removeById(id));
    }
}