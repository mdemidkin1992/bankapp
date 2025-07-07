package ru.mdemidkin.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.gateway.dto.SignupRequest;
import ru.mdemidkin.gateway.model.User;
import ru.mdemidkin.gateway.repository.UserRepository;

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

    private void hashPassword(@NonNull User user) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

}
