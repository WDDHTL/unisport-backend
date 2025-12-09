package com.unisport.interceptor;

import com.unisport.common.BusinessException;
import com.unisport.common.UserContext;
import com.unisport.config.JwtProperties;
import com.unisport.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();

        if (isWhitelist(requestUri)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("未登录或Authorization头部缺失，请求路径：{}", requestUri);
            throw new BusinessException(40101, "您尚未登录，请先登录");
        }

        String token = authHeader.substring(7);

        try {
            Long userId = JwtUtil.getUserIdFromToken(token, jwtProperties.getSecret());
            if (userId == null) {
                log.warn("Token中未包含用户ID，请求路径：{}", requestUri);
                throw new BusinessException(40101, "登录状态已失效，请重新登录");
            }

            UserContext.setUserId(userId);
            return true;
        } catch (RuntimeException e) {
            log.warn("Token解析或校验失败，请求路径：{}，原因：{}", requestUri, e.getMessage());
            throw new BusinessException(40101, "登录状态已失效，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isWhitelist(String uri) {
        if (uri.startsWith("/auth/login") || uri.startsWith("/auth/register")) {
            return true;
        }
        if (uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/doc.html")) {
            return true;
        }
        return false;
    }
}
