package ru.mdemidkin.accounts.exception;

import lombok.Getter;
import ru.mdemidkin.accounts.dto.SignupRequest;

import java.util.List;

@Getter
public class SignupRequestException extends RuntimeException {

    private final SignupRequest user;
    private final List<String> errors;

    public SignupRequestException(SignupRequest user, List<String> errors) {
        this.user = user;
        this.errors = errors;
    }
}
