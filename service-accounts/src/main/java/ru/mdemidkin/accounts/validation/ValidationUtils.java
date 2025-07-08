package ru.mdemidkin.accounts.validation;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.mdemidkin.accounts.dto.EditAccountsRequest;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.libdto.CashRequest;

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
        if (editAccountsRequest.getBirthdate() != null && !editAccountsRequest.getBirthdate().isBlank()) {
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

    public static List<String> validateEditCashRequest(@NonNull CashRequest cashRequest) {
        List<String> errors = new ArrayList<>();

        if (cashRequest.getCurrency() == null || cashRequest.getCurrency().isBlank()) {
            errors.add("Валюта не должна быть пустой");
        }
        if (cashRequest.getValue() == null) {
            errors.add("Введите значение больше 0");
        }
        if (cashRequest.getAction() == null) {
            errors.add("Действие не должно быть пустым");
        }
        return errors;
    }

}
