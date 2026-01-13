package com.unisport.WebSocket;

import com.unisport.config.SpringContext;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

public class WsAuthConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response) {

        // 1) Origin 白名单校验（防 CSWSH）
        String origin = firstHeaderIgnoreCase(request.getHeaders(), "Origin");
        if (origin == null || !isAllowedOrigin(origin)) {
            sec.getUserProperties().put("ws_auth_error", "bad_origin");
            return;
        }

        // 2) 从 Cookie 读 HttpOnly token（登录时 Set-Cookie 写入）
        String cookieHeader = firstHeaderIgnoreCase(request.getHeaders(), "Cookie");
        String token = readCookie(cookieHeader, "ACCESS_TOKEN"); // 你的cookie名

        if (token == null || token.isEmpty()) {
            sec.getUserProperties().put("ws_auth_error", "no_cookie_token");
            return;
        }

        sec.getUserProperties().put("token", token);
    }

    private boolean isAllowedOrigin(String origin) {
        // 从配置读取：ws.allowed-origins[0..]
        // Spring 的 list 属性用逗号拼出来读也行；这里用最简单的 contains 判断
        String allowed0 = SpringContext.getProperty("ws.allowed-origins[0]");
        if (allowed0 == null) {
            // 没配置就建议拒绝（更安全）；你也可以改成放行
            return false;
        }

        // 逐个读（你也可以改成自己绑定@ConfigurationProperties）
        for (int i = 0; ; i++) {
            String v = SpringContext.getProperty("ws.allowed-origins[" + i + "]");
            if (v == null) break;
            if (v.equals(origin)) return true;
        }
        return false;
    }

    private static String firstHeaderIgnoreCase(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                List<String> v = e.getValue();
                return (v == null || v.isEmpty()) ? null : v.get(0);
            }
        }
        return null;
    }

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
