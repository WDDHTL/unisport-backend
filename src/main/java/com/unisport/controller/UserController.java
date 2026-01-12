package com.unisport.controller;

import com.unisport.common.PageResult;
import com.unisport.common.Result;
import com.unisport.dto.AddEducationDTO;
import com.unisport.dto.UpdateUserDTO;
import com.unisport.service.EducationService;
import com.unisport.service.UserService;
import com.unisport.vo.EducationVO;
import com.unisport.vo.FollowUserVO;
import com.unisport.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User info endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户信息接口")
public class UserController {

    private final UserService userService;
    private final EducationService educationService;

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "用于用户主页、帖子作者信息展示")
    public Result<UserProfileVO> getUserProfile(@PathVariable("id") Long userId) {
        log.info("接到获取用户信息请求，userId={}", userId);
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "只能修改自己的基础信息，未提交字段保持不变")
    public Result<UserProfileVO> updateUserProfile(@PathVariable("id") Long userId,
                                                   @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("接到更新用户信息请求，userId={}", userId);
        UserProfileVO profile = userService.updateUserProfile(userId, updateUserDTO);
        return Result.success(profile);
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "关注用户", description = "不能关注自己，防止重复关注")
    public Result<Void> followUser(@PathVariable("id") Long targetUserId) {
        log.info("接到关注用户请求，targetUserId={}", targetUserId);
        userService.followUser(targetUserId);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注", description = "只能取消已关注的用户")
    public Result<Void> unfollowUser(@PathVariable("id") Long targetUserId) {
        log.info("接到取消关注请求，targetUserId={}", targetUserId);
        userService.unfollowUser(targetUserId);
        return Result.success("取消关注成功", null);
    }

    @GetMapping("/{id}/following")
    @Operation(summary = "获取关注列表", description = "支持current/size分页参数，默认1/20")
    public Result<PageResult<FollowUserVO>> listFollowing(@PathVariable("id") Long userId,
                                                          @RequestParam(value = "current", defaultValue = "1") Long current,
                                                          @RequestParam(value = "size", defaultValue = "20") Long size) {
        log.info("查询用户的关注列表，userId={}，current={}，size={}", userId, current, size);
        PageResult<FollowUserVO> page = userService.getFollowingList(userId, current, size);
        return Result.success(page);
    }

    @GetMapping("/{id}/educations")
    @Operation(summary = "获取用户教育经历列表", description = "按主要教育经历优先、创建时间倒序排列")
    public Result<List<EducationVO>> listUserEducations(@PathVariable("id") Long userId) {
        log.info("接到获取用户教育经历列表请求，userId={}", userId);
        List<EducationVO> educations = educationService.listUserEducations(userId);
        return Result.success(educations);
    }

    @PostMapping("/educations")
    @Operation(summary = "添加教育经历", description = "需登录，学号/学校/学院信息验证通过后新增教育经历")
    public Result<EducationVO> addEducation(@Valid @RequestBody AddEducationDTO addEducationDTO) {
        log.info("接到添加教育经历请求");
        EducationVO education = educationService.addEducation(addEducationDTO);
        return Result.success("添加成功", education);
    }
}
