package ru.mdemidkin.accounts.validation;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.mdemidkin.accounts.dto.EditAccountsRequest;
import ru.mdemidkin.accounts.dto.EditPasswordRequest;
import ru.mdemidkin.accounts.model.Account;
import ru.mdemidkin.libdto.CashAction;
import ru.mdemidkin.libdto.CashRequest;

import java.math.BigDecimal;
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

    public static List<String> validateEditCashRequest(Account account, @NonNull CashRequest cashRequest) {
        List<String> errors = new ArrayList<>();
        if (account == null) {
            errors.add("Нет счета в валюте " + cashRequest.getCurrency());
            return errors;
        }
        BigDecimal subtract = new BigDecimal(cashRequest.getValue());
        if (subtract.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Введите значение больше 0");
            return errors;
        }
        if (cashRequest.getAction() == CashAction.GET) {
            if (account.getBalance().subtract(subtract).compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Не достаточно средств для снятия "
                        + cashRequest.getCurrency() + " "
                        + cashRequest.getValue());
            }
        }
        return errors;
    }

}
