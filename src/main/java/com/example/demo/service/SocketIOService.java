// TẠO TỆP MỚI: service/SocketIOService.java

package com.example.demo.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct; // (Quan trọng: dùng jakarta.* cho Spring Boot 3)
import jakarta.annotation.PreDestroy;  // (Quan trọng: dùng jakarta.* cho Spring Boot 3)
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j; // (Dùng để log)

// Import các bean xác thực
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class SocketIOService {

    private final SocketIOServer server;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    
    // Map để lưu: String (CognitoSub) -> SocketIOClient (Session)
    private final ConcurrentHashMap<String, SocketIOClient> connectedClients = new ConcurrentHashMap<>();

    @Autowired
    public SocketIOService(SocketIOServer server, JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.server = server;
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        
        // Thêm các trình lắng nghe (listeners)
        this.server.addConnectListener(onConnect());
        this.server.addDisconnectListener(onDisconnect());
    }

    private ConnectListener onConnect() {
        return client -> {
            // Lấy token từ query param (theo tài liệu của bạn)
            String token = client.getHandshakeData().getSingleUrlParam("token");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Bỏ "Bearer "
            }

            if (StringUtils.hasText(token)) {
                try {
                    // 1. Xác thực token (giống hệt logic của JwtAuthChannelInterceptor)
                    Jwt jwt = jwtDecoder.decode(token);
                    Authentication auth = jwtAuthenticationConverter.convert(jwt);
                    String cognitoSub = auth.getName(); // Đây là cognitoSub
                    
                    // 2. Lưu client này lại theo cognitoSub
                    connectedClients.put(cognitoSub, client);
                    log.info("Socket.IO: Client [{}] connected. User: [{}].", client.getSessionId(), cognitoSub);
                    
                    // (Bạn cũng có thể lắng nghe các event 'join_room' ở đây nếu cần)
                    // client.addEventListener("join_room", (roomData) -> { ... });

                } catch (Exception e) {
                    log.warn("Socket.IO: Client [{}] connection failed. Invalid token.", client.getSessionId());
                    client.disconnect();
                }
            } else {
                 log.warn("Socket.IO: Client [{}] connection failed. Missing token.", client.getSessionId());
                 client.disconnect();
            }
        };
    }

    private DisconnectListener onDisconnect() {
        return client -> {
            // Xóa client khỏi map khi họ ngắt kết nối
            connectedClients.entrySet().stream()
                .filter(entry -> entry.getValue().getSessionId().equals(client.getSessionId()))
                .map(entry -> entry.getKey())
                .findFirst()
                .ifPresent(cognitoSub -> {
                    connectedClients.remove(cognitoSub);
                    log.info("Socket.IO: Client [{}] disconnected. User: [{}].", client.getSessionId(), cognitoSub);
                });
        };
    }

    /**
     * Hàm này được BroadcastServiceImpl gọi để gửi tin nhắn đến một user cụ thể.
     * @param cognitoSub ID của người nhận
     * @param eventName Tên sự kiện (FE sẽ lắng nghe tên này)
     * @param data Nội dung (payload)
     */
    public void sendEventToUser(String cognitoSub, String eventName, Object data) {
        SocketIOClient client = connectedClients.get(cognitoSub);
        if (client != null && client.isChannelOpen()) {
            // Gửi sự kiện (emit)
            client.sendEvent(eventName, data);
        } else {
            log.warn("Socket.IO: No active client found for user [{}]. Message for event [{}] not sent.", cognitoSub, eventName);
        }
    }

    // Bật server khi Spring khởi động
    @PostConstruct
    private void startServer() {
        server.start();
        log.info("Socket.IO server started on port {}...", server.getConfiguration().getPort());
    }

    // Tắt server khi Spring tắt
    @PreDestroy
    private void stopServer() {
        server.stop();
        log.info("Socket.IO server stopped.");
    }
}