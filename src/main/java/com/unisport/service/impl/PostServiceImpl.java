package com.unisport.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.dto.CreatePostDTO;
import com.unisport.dto.PostQueryDTO;
import com.unisport.entity.Category;
import com.unisport.entity.Match;
import com.unisport.entity.Post;
import com.unisport.entity.User;
import com.unisport.mapper.CategoryMapper;
import com.unisport.mapper.PostMapper;
import com.unisport.mapper.UserMapper;
import com.unisport.service.PostService;
import com.unisport.vo.MatchVO;
import com.unisport.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 * 实现帖子发布等业务逻辑
 *
 * @author UniSport Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    /**
     * 发布帖子
     * 
     * 业务流程：
     * 1. 从ThreadLocal获取当前登录用户ID
     * 2. 验证运动分类是否存在
     * 3. 检查发帖频率限制（1分钟内最多3条）
     * 4. 将图片列表转为JSON字符串存储
     * 5. 创建帖子记录
     * 6. 返回发布成功的帖子实体
     *
     * @param createPostDTO 发布帖子请求数据
     * @return 发布成功的帖子实体
     * @throws BusinessException 40004-运动分类不存在, 40901-发帖频率超限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Post createPost(CreatePostDTO createPostDTO) {
        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        log.info("开始处理发布帖子，用户ID：{}", userId);

        // 获取当前用户的学校id
        Long schoolId = UserContext.getSchoolId();
        if (schoolId == null) {
            User currentUser = userMapper.selectById(userId);
            if (currentUser == null) {
                throw new BusinessException(40401, "用户不存在");
            }
            schoolId = currentUser.getSchoolId();
        }
        if (schoolId == null) {
            throw new BusinessException(40004, "学校信息缺失");
        }

        // 3. 检查发帖频率限制（1分钟内最多3条）
        validatePostFrequency(userId);

        // 4. 构建帖子实体
        Post post = buildPost(userId, createPostDTO.getCategoryId(), createPostDTO);
        post.setSchoolId(schoolId);

        // 5. 保存帖子到数据库
        int rows = postMapper.insert(post);
        if (rows <= 0) {
            log.error("帖子发布失败，数据库插入失败，用户ID：{}", userId);
            throw new BusinessException(50001, "发布失败，请稍后重试");
        }

        log.info("帖子发布成功，帖子ID：{}，用户ID：{}", post.getId(), userId);
        return post;
    }

    @Override
    public List<PostVO> getPostList(PostQueryDTO postQueryDTO) {
        log.info("查询帖子列表，参数：{}", postQueryDTO);

        // ===== 1. 严格安全校验 =====
        // 1.1 用户校验
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        Long schoolId = UserContext.getSchoolId();
        if (schoolId == null) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            schoolId = user.getSchoolId();
        }

        // 1.3 分类ID校验
        if (postQueryDTO.getCategoryId() == null) {
            throw new BusinessException("分类ID不能为空");
        }

        log.info("查询帖子列表，用户ID：{}, 学校: {}, 分类: {}", userId, schoolId, postQueryDTO.getCategoryId());

        // 构建查询条件（MyBatis-Plus会自动添加 deleted=0 条件）
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getSchoolId, schoolId)
                   .eq(Post::getCategoryId, postQueryDTO.getCategoryId())
                   .orderByDesc(Post::getCreatedAt);

        // 执行分页查询
        List<Post> posts = postMapper.selectList(queryWrapper);

        // 转换为VO
        List<PostVO> voList = posts.stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);
            
            // 查询用户信息
            User postUser = userMapper.selectById(post.getUserId());
            if (postUser != null) {
                vo.setUserName(postUser.getNickname());
                vo.setUserAvatar(postUser.getAvatar());
            }
            
            vo.setLiked(false); // TODO 默认未点赞
            return vo;
        }).collect(Collectors.toList());

        log.info("查询到 {} 条帖子数据", voList.size());
        return voList;
    }

    /**
     * 验证运动分类是否存在并获取分类ID
     * 
     * @param categoryCode 运动分类代码
     * @return 分类ID
     * @throws BusinessException 40004-运动分类不存在
     */
    private Integer validateAndGetCategoryId(String categoryCode) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getCode, categoryCode)
                   .eq(Category::getStatus, 1); // 只查询启用状态的分类

        Category category = categoryMapper.selectOne(queryWrapper);
        
        if (category == null) {
            log.warn("运动分类不存在或已禁用：{}", categoryCode);
            throw new BusinessException(40004, "运动分类不存在");
        }
        
        log.debug("运动分类验证通过：代码={}，ID={}", categoryCode, category.getId());
        return category.getId();
    }

    /**
     * 检查发帖频率限制
     * 规则：1分钟内最多发布3条帖子
     * 
     * @param userId 用户ID
     * @throws BusinessException 40901-发帖频率超限
     */
    private void validatePostFrequency(Long userId) {
        // 计算1分钟前的时间点
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        
        // 查询1分钟内发布的帖子数量
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId)
                   .ge(Post::getCreatedAt, oneMinuteAgo);
        
        Long count = postMapper.selectCount(queryWrapper);
        
        if (count >= 3) {
            log.warn("发帖频率超限，用户ID：{}，1分钟内已发布{}条帖子", userId, count);
            throw new BusinessException(40901, "发帖过于频繁，请稍后再试");
        }
        
        log.debug("发帖频率检查通过，用户ID：{}，1分钟内已发布{}条帖子", userId, count);
    }

    /**
     * 构建帖子实体对象
     * 
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @param createPostDTO 发布帖子请求数据
     * @return 帖子实体对象
     */
    private Post buildPost(Long userId, Integer categoryId, CreatePostDTO createPostDTO) {
        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(categoryId);
        post.setContent(createPostDTO.getContent());
        
        // 将图片列表转为JSON字符串存储
        if (!CollectionUtils.isEmpty(createPostDTO.getImages())) {
            String imagesJson = JSONUtil.toJsonStr(createPostDTO.getImages());
            post.setImages(imagesJson);
            log.debug("帖子图片数量：{}", createPostDTO.getImages().size());
        }
        
        // 设置初始计数
        post.setLikesCount(0);
        post.setCommentsCount(0);
        
        // 设置状态为正常
        post.setDeleted(0);
        
        log.debug("帖子实体构建完成，用户ID：{}", userId);
        return post;
    }
}
