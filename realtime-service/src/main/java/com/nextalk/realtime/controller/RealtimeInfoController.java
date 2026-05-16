package com.nextalk.realtime.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeInfoController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "endpoint", "/ws",
                "applicationPrefix", "/app",
                "topics", List.of(
                        "/topic/rooms/{roomId}/typing",
                        "/topic/rooms/{roomId}/reads",
                        "/topic/rooms/{roomId}/reactions",
                        "/topic/rooms/{roomId}/events",
                        "/topic/presence"
                ),
                "sendDestinations", List.of(
                        "/app/typing",
                        "/app/read",
                        "/app/presence",
                        "/app/reaction",
                        "/app/room-event"
                )
        );
    }
}
