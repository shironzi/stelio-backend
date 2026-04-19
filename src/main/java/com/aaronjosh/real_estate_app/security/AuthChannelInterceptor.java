package com.aaronjosh.real_estate_app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AuthChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null)
            return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            String userId = jwtService.extractUserId(token);

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && jwtService.isAccessTokenValid(token)) {
                UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                        userId, null, jwtService.extractAuthorities(token));
                accessor.setUser(user);
            } else {
                throw new MessageDeliveryException("Invalid Token");
            }
        }

        return message;
    }
}
