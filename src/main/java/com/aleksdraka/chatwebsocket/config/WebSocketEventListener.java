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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSocketEventListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    private final ConcurrentMap<String, ConcurrentMap<String, String>> roomUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getFirstNativeHeader("username");
        String roomId = headerAccessor.getFirstNativeHeader("roomId");
        String sessionId = headerAccessor.getSessionId();

        if (username != null) {
            roomUsers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, username);
            log.info("User connected: {}", username);

            int userCount = roomUsers.get(roomId).size();
            messagingTemplate.convertAndSend("/topic/" + roomId + "/userCount", userCount);
            log.info("Users in room {} after new connection: {}", roomId, roomUsers.get(roomId));
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        roomUsers.forEach((roomId, users) -> {
            if (users.containsKey(sessionId)) {
                String username = users.remove(sessionId);
                log.info("User {} disconnected from room {}", username, roomId);

                ChatMessage chatMessage = ChatMessage.builder()
                        .type(MessageType.LEAVE)
                        .sender(username)
                        .build();
                messagingTemplate.convertAndSend("/topic/" + roomId, chatMessage);

                messagingTemplate.convertAndSend("/topic/" + roomId + "/userCount", users.size());
                log.info("Users in room {} after disconnect: {}", roomId, users);

                if (users.isEmpty()) {
                    roomUsers.remove(roomId);
                    log.info("Room {} is now empty and removed.", roomId);
                }
            }
        });
    }
}
