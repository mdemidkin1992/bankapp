package ru.mdemidkin.accounts.validation;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.mdemidkin.accounts.dto.EditAccountsRequest;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class ValidationUtils {

    public static List<String> validatePasswordRequest(@NonNull EditPasswordRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            errors.add("Пароль не должен быть пустым");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            errors.add("Повторный пароль не должен быть пустым");
        }
        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            errors.add("Пароли должны совпадать");
        }

        return errors;
    }

    public static List<String> validateEditUserAccountsRequest(@NonNull EditAccountsRequest editAccountsRequest) {
        List<String> errors = new ArrayList<>();

        if (editAccountsRequest.getName() == null || editAccountsRequest.getName().isBlank()) {
            errors.add("Имя не должно быть пустым");
        }

        if (editAccountsRequest.getBirthdate() == null || editAccountsRequest.getBirthdate().isBlank()) {
            errors.add("Дата рождения не должна быть пустой");
        } else {
            try {
                LocalDate birthdate = LocalDate.parse(editAccountsRequest.getBirthdate());
                if (birthdate.isAfter(LocalDate.now().minusYears(18))) {
                    errors.add("Возраст должен быть не менее 18 лет");
                }
            } catch (Exception e) {
                errors.add("Неверный формат даты рождения");
            }
        }

        return errors;
    }
}
