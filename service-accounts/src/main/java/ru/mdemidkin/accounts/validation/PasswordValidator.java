package ru.mdemidkin.accounts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.accounts.exception.EditPasswordRequestException;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<ValidPassword, EditPasswordRequest> {

    @Override
    public boolean isValid(EditPasswordRequest value, ConstraintValidatorContext context) {
        List<String> errors = new ArrayList<>();

        if (value.getPassword() == null || value.getPassword().isBlank()) {
            errors.add("Пароль не должен быть пустым");
        }
        if (value.getConfirmPassword() == null || value.getConfirmPassword().isBlank()) {
            errors.add("Повторный пароль не должен быть пустым");
        }
        if (value.getPassword() != null && !value.getPassword().equals(value.getConfirmPassword())) {
            errors.add("Пароли должны совпадать");
        }

        if (!errors.isEmpty()) {
            throw new EditPasswordRequestException(value, errors);
        }

        return true;
    }
}
