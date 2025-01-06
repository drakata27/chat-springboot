package com.aleksdraka.chatwebsocket.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RoomController {
    @PostMapping("/api/rooms/create")
    public ResponseEntity<String> createRoom() {
        String roomId = UUID.randomUUID().toString();
        return ResponseEntity.ok(roomId);
    }
}
