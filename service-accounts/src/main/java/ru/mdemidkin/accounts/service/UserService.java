package ru.mdemidkin.accounts.service;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.accounts.dto.SignupRequest;
import ru.mdemidkin.accounts.model.User;
import ru.mdemidkin.accounts.repository.UserRepository;
import ru.mdemidkin.libdto.UserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> registerNewUser(SignupRequest signupRequest) {
        User createUser = User.builder()
                .name(signupRequest.getName())
                .login(signupRequest.getLogin())
                .password(signupRequest.getPassword())
                .birthdate(signupRequest.getBirthdate())
                .build();

        hashPassword(createUser);
        return userRepository.save(createUser);
    }

    public Mono<User> editPassword(String login, EditPasswordRequest editPasswordRequest) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь не найден: " + login)))
                .flatMap(user -> {
                    hashPassword(user);
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

    private void hashPassword(@NonNull User user) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

}
