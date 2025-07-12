package ru.mdemidkin.gateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.account.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomReactiveUserDetailsServiceTest {

    @Mock
    private WebClient accountsClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CustomReactiveUserDetailsService userDetailsService;

    private static final String URL_TEMPLATE =
            "http://service-accounts/auth/users/{username}";

    @BeforeEach
    void setUp() {
        when(accountsClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec
                .uri(URL_TEMPLATE, "john"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
    }

    @Test
    void findByUsername_success() {
        UserDto userDto = UserDto.builder()
                .login("john")
                .password("encodedPass")
                .role("ROLE_USER")
                .build();
        when(responseSpec.bodyToMono(UserDto.class))
                .thenReturn(Mono.just(userDto));

        UserDetails userDetails = userDetailsService
                .findByUsername("john").block();

        assertNotNull(userDetails);
        assertEquals("john", userDetails.getUsername());
        assertEquals("encodedPass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void findByUsername_notFound() {
        when(responseSpec.bodyToMono(UserDto.class))
                .thenReturn(Mono.error(new RuntimeException("Not found")));

        assertThrows(RuntimeException.class,
                () -> userDetailsService.findByUsername("john").block());
    }
}
