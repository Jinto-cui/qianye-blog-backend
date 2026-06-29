package com.qianye.blog.web.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.model.dto.AdminCommentDto;
import com.qianye.blog.web.service.AdminCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台评论接口。
 *
 * @author: Jinto Cui
 * @desc: 提供正式后台评论分页、搜索和删除能力，区别于 legacy 裸 CRUD 接口。
 * @date: 2026/06/30 00:31
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminCommentController {

    @Autowired
    private AdminCommentService adminCommentService;

    /**
     * 分页查询评论列表。
     */
    @GetMapping("/comments")
    public Result<Page<AdminCommentDto>> listComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) String keyword) {
        return ResultUtils.success(adminCommentService.listComments(page, size, postId, keyword));
    }

    /**
     * 查询评论总数。
     */
    @GetMapping("/comments/count")
    public Result<Long> countComments(@RequestParam(required = false) Long postId,
                                      @RequestParam(required = false) String keyword) {
        return ResultUtils.success(adminCommentService.countComments(postId, keyword));
    }

    /**
     * 删除评论。
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminCommentService.deleteComment(id);
        return ResultUtils.success(null);
    }
}
