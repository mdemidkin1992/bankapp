package ru.mdemidkin.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.User;
import ru.mdemidkin.accounts.repository.UserRepository;
import ru.mdemidkin.libdto.settings.EditAccountsRequest;
import ru.mdemidkin.libdto.settings.EditPasswordRequest;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.account.UserDto;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> registerNewUser(SignupRequest signupRequest) {
        User createUser = User.builder()
                .name(signupRequest.getName())
                .login(signupRequest.getLogin())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .birthdate(signupRequest.getDate())
                .role("USER")
                .build();
        return userRepository.save(createUser);
    }

    public Mono<User> editPassword(String login, EditPasswordRequest editPasswordRequest) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(editPasswordRequest.getPassword()));
                    return userRepository.save(user);
                });
    }

    public Mono<UserDto> findByUsername(String username) {
        return userRepository.findByLogin(username)
                .map(this::mapToUserDto)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + username)));
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .login(user.getLogin())
                .password(user.getPassword())
                .role(user.getRole())
                .birthdate(user.getBirthdate())
                .name(user.getName())
                .build();

    }

    public Flux<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> updateUserInfo(String login, EditAccountsRequest editAccountsRequest) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> {
                    if (editAccountsRequest.getName() != null && !editAccountsRequest.getName().isBlank()) {
                        user.setName(editAccountsRequest.getName());
                    }
                    if (editAccountsRequest.getBirthdate() != null && !editAccountsRequest.getBirthdate().isBlank()) {
                        LocalDate birthdate = LocalDate.parse(editAccountsRequest.getBirthdate());
                        user.setBirthdate(birthdate);
                    }
                    return userRepository.save(user);
                });

    }
}
