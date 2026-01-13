package com.unisport.WebSocket;

import com.unisport.config.SpringContext;
import com.unisport.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * WebSocket服务
 */
@Component
@ServerEndpoint(value = "/ws", configurator = WsAuthConfigurator.class)
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        // 0) 握手阶段是否拦截了
        if (config.getUserProperties().get("ws_auth_error") != null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Forbidden"));
            return;
        }

        // 1) token 从 Cookie 来（握手阶段放入 userProperties）
        String token = (String) config.getUserProperties().get("token");
        token = stripBearer(token); // 如果你 cookie 里存的是 "Bearer xxx" 就需要；否则无所谓

        // 2) secret 从配置文件读取
        String secret = SpringContext.getProperty("jwt.secret");
        if (secret == null || secret.isEmpty()) {
            // 服务端配置错误
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Server config error"));
            return;
        }

        // 3) 校验token + 解析 userId（parseToken 内部已验签/验过期）
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token, secret);
        } catch (Exception e) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }

        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }

        String uid = String.valueOf(userId);
        session.getUserProperties().put("userId", uid);

        // 4) 保存连接（后连覆盖前连：可选策略）
        Session old = sessionMap.put(uid, session);
        if (old != null && old.isOpen() && old != session) {
            try { old.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Replaced")); } catch (Exception ignore) {}
        }

        System.out.println("客户端 userId=" + uid + " 建立WS连接");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // 你当前只是打印即可
        String userId = (String) session.getUserProperties().get("userId");
        System.out.println("收到来自客户端 userId=" + userId + " msg=" + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId != null) {
            // 防误删：只移除当前这个 session
            sessionMap.remove(userId, session);
        }
        System.out.println("WS断开 userId=" + userId + " reason=" + reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String userId = (String) session.getUserProperties().get("userId");
        if (userId != null) {
            sessionMap.remove(userId, session);
        }
        System.out.println("WS异常 userId=" + userId + " err=" + error.getMessage());
    }

    /**
     * best-effort 单播：失败不抛异常，不影响 HTTP 主流程
     */
    public static boolean trySendToUser(Long recipientId, String message) {
        String reciverIdStr = recipientId.toString();
        Session session = sessionMap.get(reciverIdStr);
        if (session == null || !session.isOpen()) return false;

        // 用异步发送，失败回调清理；不影响调用方线程
        session.getAsyncRemote().sendText(message, result -> {
            if (!result.isOK()) {
                sessionMap.remove(reciverIdStr, session);
            }
        });
        return true;
    }

    private String stripBearer(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return token.substring(7).trim();
        }
        return token;
    }

}
