package ru.mdemidkin.notifications.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.notifications.model.Notification;
import ru.mdemidkin.notifications.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private final String login = "user1";
    private final String message = "Test message";

    @Test
    void notify_savesAndReturnsNotification() {
        Notification saved = Notification.builder()
                .login(login)
                .message(message)
                .time(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Mono.just(saved));

        Notification result = notificationService.notify(login, message).block();

        assertNotNull(result);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification toSave = notificationCaptor.getValue();
        assertEquals(login,   toSave.getLogin());
        assertEquals(message, toSave.getMessage());
        assertNotNull(toSave.getTime());

        assertEquals(saved, result);
    }

    @Test
    void notify_repositoryErrorPropagates() {
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificationService.notify(login, message).block());
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void getUserNotifications_returnsFluxFromRepository() {
        Notification n1 = Notification.builder()
                .login(login).message("m1").time(LocalDateTime.now()).build();
        Notification n2 = Notification.builder()
                .login(login).message("m2").time(LocalDateTime.now()).build();

        when(notificationRepository.findAllByLogin(login))
                .thenReturn(Flux.just(n1, n2));

        List<Notification> list = notificationService
                .getUserNotifications(login)
                .collectList()
                .block();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(List.of(n1, n2), list);
    }

    @Test
    void getUserNotifications_repositoryErrorPropagates() {
        when(notificationRepository.findAllByLogin(login))
                .thenReturn(Flux.error(new RuntimeException("find error")));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificationService.getUserNotifications(login).collectList().block());
        assertEquals("find error", ex.getMessage());
    }
}
