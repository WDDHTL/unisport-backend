package com.unisport.interceptor;

import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.config.JwtProperties;
import com.unisport.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final Pattern INVITE_DETAIL_PATTERN = Pattern.compile("^/invites/\\d+/?$");

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        // 去掉 context-path，避免 /api 前缀影响白名单匹配
        String path = requestUri.substring(request.getContextPath().length());

        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        boolean optionalAuth = isOptionalAuthPath(path, request.getMethod());
        if (isWhitelist(path, request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("收到请求 - URI: {}, Method: {}, Authorization: {}", requestUri, request.getMethod(), authHeader != null ? "存在" : "不存在");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            if (optionalAuth) {
                return true;
            }
            log.warn("未登录或Authorization头部缺失，请求路径：{}", requestUri);
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }

        String token = authHeader.substring(7);

        try {
            String secret = jwtProperties.getSecret();
            Long userId = JwtUtil.getUserIdFromToken(token, secret);
            Long schoolId = JwtUtil.getSchoolIdFromToken(token, secret);
            if (userId == null) {
                log.warn("Token中未包含用户ID，请求路径：{}", requestUri);
                throw new BusinessException(40101, "登录状态已失效，请重新登录");
            }

            UserContext.setCurrentUser(userId, schoolId);
            return true;
        } catch (RuntimeException e) {
            if (optionalAuth) {
                log.warn("Token解析或校验失败，将以匿名访问，请求路径：{}，原因：{}", requestUri, e.getMessage());
                return true;
            }
            log.warn("Token解析或校验失败，请求路径：{}，原因：{}", requestUri, e.getMessage());
            throw new BusinessException(40101, "登录状态已失效，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isWhitelist(String uri, String method) {
        // 鉴权白名单（无需登录）
        if (uri.startsWith("/auth/login") || uri.startsWith("/auth/register")) {
            return true;
        }
        // 学校列表、学号列表、学号校验接口对未登录用户开放
        if ("GET".equalsIgnoreCase(method)) {
            if ("/schools".equals(uri)) {
                return true;
            }
            if ("/departments".equals(uri)) {
                return true;
            }
            if ("/students".equals(uri) || "/students/validate".equals(uri)) {
                return true;
            }
        }
        // Swagger 文档
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/doc.html")) {
            return true;
        }
        return false;
    }

    private boolean isOptionalAuthPath(String uri, String method) {
        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }
        return INVITE_DETAIL_PATTERN.matcher(uri).matches();
    }
}
