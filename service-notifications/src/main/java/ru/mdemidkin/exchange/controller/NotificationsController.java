package ru.mdemidkin.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.exchange.model.Notification;
import ru.mdemidkin.exchange.service.NotificationService;
import ru.mdemidkin.libdto.NotificationDto;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationService notificationService;

    @GetMapping("/api/{login}/notifications")
    public Flux<NotificationDto> getUserNotifications(@PathVariable String login) {
        return notificationService.getUserNotifications(login)
                .collectSortedList(Comparator.comparing(Notification::getTime).reversed())
                .flatMapMany(list -> Flux.fromIterable(
                        list.stream()
                                .limit(10)
                                .collect(Collectors.toList())
                ))
                .map(this::mapToDto);
    }

    @PostMapping("/api/{login}/notifications")
    public Mono<NotificationDto> postUserNotification(@PathVariable String login,
                                                      @RequestBody String message) {
        return notificationService.notify(login, message)
                .map(this::mapToDto);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .message(notification.getMessage())
                .build();

    }
}
