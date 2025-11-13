// TẠO TỆP MỚI: config/SocketIOConfig.java

package com.example.demo.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Value("${socket.host:0.0.0.0}") // Dùng 0.0.0.0 để chạy được trên Render
    private String host;

    @Value("${socket.port:9092}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        
        // (Chúng ta sẽ xử lý xác thực khi client 'onConnect'
        // thay vì ở config listener)
        
        return new SocketIOServer(config);
    }
}