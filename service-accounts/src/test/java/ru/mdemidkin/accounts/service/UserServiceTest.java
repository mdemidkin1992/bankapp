package ru.mdemidkin.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.User;
import ru.mdemidkin.accounts.repository.UserRepository;
import ru.mdemidkin.libdto.account.UserDto;
import ru.mdemidkin.libdto.settings.EditAccountsRequest;
import ru.mdemidkin.libdto.settings.EditPasswordRequest;
import ru.mdemidkin.libdto.signup.SignupRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .login("user1")
                .name("Old Name")
                .password("oldpass")
                .birthdate(LocalDate.of(1990, 1, 1))
                .role("USER")
                .build();
    }

    @Test
    void registerNewUser_success() {
        SignupRequest req = SignupRequest.builder()
                .login("newuser")
                .name("New User")
                .password("rawpass")
                .birthdate("1985-05-20")
                .build();

        when(passwordEncoder.encode("rawpass")).thenReturn("encoded");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        User result = userService.registerNewUser(req).block();
        assertNotNull(result);
        then(userRepository).should().save(captor.capture());
        User saved = captor.getValue();
        assertEquals("newuser", saved.getLogin());
        assertEquals("New User", saved.getName());
        assertEquals("encoded", saved.getPassword());
        assertEquals("USER", saved.getRole());
    }

    @Test
    void editPassword_userNotFound() {
        when(userRepository.findByLogin("nope")).thenReturn(Mono.empty());
        EditPasswordRequest req = new EditPasswordRequest();
        req.setPassword("x");
        req.setConfirmPassword("x");

        assertThrows(UsernameNotFoundException.class,
                () -> userService.editPassword("nope", req).block());
    }

    @Test
    void findByUsername_success() {
        when(userRepository.findByLogin("user1")).thenReturn(Mono.just(existingUser));

        UserDto dto = userService.findByUsername("user1").block();
        assertNotNull(dto);
        assertEquals(existingUser.getLogin(), dto.getLogin());
        assertEquals(existingUser.getName(), dto.getName());
        assertEquals(existingUser.getRole(), dto.getRole());
        assertEquals(existingUser.getBirthdate(), dto.getBirthdate());
    }

    @Test
    void findByUsername_notFound() {
        when(userRepository.findByLogin("nope")).thenReturn(Mono.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.findByUsername("nope").block());
    }

    @Test
    void findAllUsers_returnsList() {
        User u2 = new User();
        when(userRepository.findAll()).thenReturn(Flux.just(existingUser, u2));

        List<User> list = userService.findAllUsers().collectList().block();
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void updateUserInfo_changeNameAndBirthdate() {
        when(userRepository.findByLogin("user1")).thenReturn(Mono.just(existingUser));
        EditAccountsRequest req = new EditAccountsRequest();
        req.setName("New Name");
        req.setBirthdate("2000-12-12");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        User updated = userService.updateUserInfo("user1", req).block();
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals(LocalDate.parse("2000-12-12"), updated.getBirthdate());
    }

    @Test
    void updateUserInfo_userNotFound() {
        when(userRepository.findByLogin("nope")).thenReturn(Mono.empty());
        EditAccountsRequest req = new EditAccountsRequest();

        assertThrows(UsernameNotFoundException.class,
                () -> userService.updateUserInfo("nope", req).block());
    }

    @Test
    void updateUserInfo_skipEmptyFields() {
        when(userRepository.findByLogin("user1")).thenReturn(Mono.just(existingUser));
        EditAccountsRequest req = new EditAccountsRequest();
        req.setName("");
        req.setBirthdate("");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        User result = userService.updateUserInfo("user1", req).block();
        assertNotNull(result);
        assertEquals("Old Name", result.getName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthdate());
    }
}
