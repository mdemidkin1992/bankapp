package ru.mdemidkin.libdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainPageRequest {
    private String login;
    private String name;
    private LocalDate birthdate;
    private List<AccountDto> accounts;
    private List<UserDto> users;
    private List<Currency> currency;
    private Object passwordErrors;
    private Object userAccountsErrors;
    private Object cashErrors;
    private Object transferErrors;
    private Object transferOtherErrors;
}
