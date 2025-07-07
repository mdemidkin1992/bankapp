package ru.mdemidkin.gateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.mdemidkin.gateway.dto.SignupRequest;
import ru.mdemidkin.gateway.exception.SignupRequestException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SignupValidator implements ConstraintValidator<ValidSignup, SignupRequest> {

    @Override
    public boolean isValid(SignupRequest value, ConstraintValidatorContext context) {
        List<String> errors = new ArrayList<>();

        if (value.getName() == null || value.getName().isBlank()) {
            errors.add("Имя пользователя не должно быть пустым");
        }
        if (value.getLogin() == null || value.getLogin().isBlank()) {
            errors.add("Логин не должен быть пустым");
        }
        if (value.getPassword() == null || value.getPassword().isBlank()) {
            errors.add("Пароль не должен быть пустым");
        }
        if (value.getConfirmPassword() == null || value.getConfirmPassword().isBlank()) {
            errors.add("Повторный пароль не должен быть пустым");
        }
        if (value.getBirthdate() == null) {
            errors.add("Дата рождения не должна быть пустой");
        }
        if (value.getPassword() != null && !value.getPassword().equals(value.getConfirmPassword())) {
            errors.add("Пароли должны совпадать");
        }
        if (value.getBirthdate() != null && LocalDate.now().isAfter(value.getBirthdate().plusYears(18L))) {
            errors.add("Возраст должен быть старше 18 лет");
        }

        if (!errors.isEmpty()) {
            throw new SignupRequestException(value, errors);
        }

        return true;
    }
}
