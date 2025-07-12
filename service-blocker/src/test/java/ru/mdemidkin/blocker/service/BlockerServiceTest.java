package ru.mdemidkin.blocker.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockerServiceTest {

    private final BlockerService blockerService = new BlockerService();

    @Test
    void shouldBlockAt21Hours() {
        LocalDateTime time = LocalDateTime.of(2025, 7, 12, 21, 0);
        Boolean result = blockerService.isBlocked(time).block();

        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void shouldNotBlockBefore21Hours() {
        LocalDateTime time = LocalDateTime.of(2025, 7, 12, 20, 59);
        Boolean result = blockerService.isBlocked(time).block();

        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    void shouldBlockAtMidnight() {
        LocalDateTime time = LocalDateTime.of(2025, 7, 13, 0, 0);
        Boolean result = blockerService.isBlocked(time).block();

        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void shouldBlockAtSixAM() {
        LocalDateTime time = LocalDateTime.of(2025, 7, 13, 6, 0);
        Boolean result = blockerService.isBlocked(time).block();

        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void shouldNotBlockAfterSixAM() {
        LocalDateTime time = LocalDateTime.of(2025, 7, 13, 7, 0);
        Boolean result = blockerService.isBlocked(time).block();

        assertNotNull(result);
        assertFalse(result);
    }
}
