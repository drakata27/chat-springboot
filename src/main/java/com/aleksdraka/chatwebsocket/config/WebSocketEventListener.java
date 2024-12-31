package com.aleksdraka.chatwebsocket.config;

import com.aleksdraka.chatwebsocket.chat.ChatMessage;
import com.aleksdraka.chatwebsocket.chat.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSocketEventListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Add to WebSocketEventListener
    private final ConcurrentMap<String, String> activeUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getFirstNativeHeader("username"); // Retrieve username from headers
        String sessionId = headerAccessor.getSessionId();

        if (username != null) {
            activeUsers.put(sessionId, username); // Add user to active map
            log.info("User connected: {}", username);

            // Optionally broadcast updated user count
            messagingTemplate.convertAndSend("/topic/userCount", activeUsers.size());

            log.info("Active Users: {}", activeUsers);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");

        if (username != null) {
            log.info("Disconnected from {}", username);
            var chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .build();
            messagingTemplate.convertAndSend("/topic/public", chatMessage);

            // Optionally broadcast updated user count
            messagingTemplate.convertAndSend("/topic/userCount", activeUsers.size());
            log.info("Active Users after disconnecting: {}", activeUsers);
        }
    }
}
