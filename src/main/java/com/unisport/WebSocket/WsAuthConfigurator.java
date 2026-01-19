package com.unisport.WebSocket;

import com.unisport.config.SpringContext;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

/**
 * WebSocket握手阶段的鉴权器：
 * 1. 校验 Origin 是否在 ws.allowed-origins[*] 白名单，防止 CSWSH。
 * 2. 从 Cookie 中提取 ACCESS_TOKEN，放入 userProperties 供 @OnOpen 读取。
 */
public class WsAuthConfigurator extends ServerEndpointConfig.Configurator {

    /**
     * 在握手阶段提前做安全校验：验证 Origin 白名单并从 Cookie 中提取 ACCESS_TOKEN 供后续 @OnOpen 使用，
     * 若校验失败则在 userProperties 中标记错误并中断握手。
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response) {

        // Step 1: 按白名单校验 Origin，未知来源直接拒绝。
        String origin = firstHeaderIgnoreCase(request.getHeaders(), "Origin");
        if (origin == null || !isAllowedOrigin(origin)) {
            sec.getUserProperties().put("ws_auth_error", "bad_origin");
            return;
        }

        // Step 2: 解析 Cookie，提取登录态 token，缺失则中断握手。
        String cookieHeader = firstHeaderIgnoreCase(request.getHeaders(), "Cookie");
        String token = readCookie(cookieHeader, "ACCESS_TOKEN"); // 你的 cookie 名称

        if (token == null || token.isEmpty()) {
            sec.getUserProperties().put("ws_auth_error", "no_cookie_token");
            return;
        }

        // 在 userProperties 中塞入 token，后续 WebSocketServer.@OnOpen 可以取出校验。
        sec.getUserProperties().put("token", token);
    }

    /**
     * 读取配置 ws.allowed-origins[*]，判断是否包含给定 Origin。
     */
    private boolean isAllowedOrigin(String origin) {
        String allowed0 = SpringContext.getProperty("ws.allowed-origins[0]");
        if (allowed0 == null) {
            // 未配置则默认拒绝，优先安全
            return false;
        }

        // 逐个读取数组配置，直到遇到空值
        for (int i = 0; ; i++) {
            String v = SpringContext.getProperty("ws.allowed-origins[" + i + "]");
            if (v == null) break;
            if (v.equals(origin)) return true;
        }
        return false;
    }

    /**
     * 不区分大小写地获取首个指定请求头。
     */
    private static String firstHeaderIgnoreCase(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                List<String> v = e.getValue();
                return (v == null || v.isEmpty()) ? null : v.get(0);
            }
        }
        return null;
    }

    /**
     * 手动解析 Cookie 头，提取指定 cookieName 的值。
     */
    private static String readCookie(String cookieHeader, String cookieName) {
        if (cookieHeader == null || cookieHeader.isEmpty()) return null;
        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(cookieName)) {
                return kv[1].trim();
            }
        }
        return null;
    }
}
