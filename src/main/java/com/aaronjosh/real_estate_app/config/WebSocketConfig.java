package com.aaronjosh.real_estate_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Clients connect here
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "https://stelio-frontend.aaronbaon1.workers.dev")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Incoming messages prefix
        registry.setApplicationDestinationPrefixes("/app");
        // Outgoing messages broker
        registry.enableSimpleBroker("/my-bookings", "/user");
    }
}
