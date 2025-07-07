package ru.mdemidkin.accounts.exception;

import lombok.Getter;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;

import java.util.List;

@Getter
public class EditPasswordRequestException extends RuntimeException {

    private final EditPasswordRequest passwordRequest;
    private final List<String> errors;

    public EditPasswordRequestException(EditPasswordRequest passwordRequest, List<String> errors) {
        this.passwordRequest = passwordRequest;
        this.errors = errors;
    }
}
