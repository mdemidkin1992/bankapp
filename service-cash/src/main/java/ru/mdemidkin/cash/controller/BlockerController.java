package ru.mdemidkin.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.service.BlockerService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping
@RequiredArgsConstructor
// todo перенести в service-blocker
public class BlockerController {

    private final BlockerService blockerService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostMapping("/api/{time}/block")
    public Mono<Boolean> postUserNotification(@PathVariable String time) {
        String decodedTime = URLDecoder.decode(time, StandardCharsets.UTF_8);
        LocalDateTime parsedTime = LocalDateTime.parse(decodedTime, FORMATTER);
        return blockerService.isBlocked(parsedTime);
    }

}
