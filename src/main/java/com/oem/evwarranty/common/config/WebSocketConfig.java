package com.oem.evwarranty.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP Message Broker Configuration.
 * Endpoints for Live GPS Telemetry (/topic/telemetry/...) and Real-time Chat (/user/queue/messages, /app/chat.sendMessage).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages to clients on destinations prefixed with /topic and /queue
        config.enableSimpleBroker("/topic", "/queue", "/user");
        // Designate the /app prefix for messages bound for methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // Designate the /user prefix for user-specific queues
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options so that alternate transports can be used if WebSocket is not available
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Plain WebSocket endpoint without SockJS for mobile / IoT clients
        registry.addEndpoint("/ws/raw")
                .setAllowedOriginPatterns("*");
    }
}
