package ru.mdemidkin.libdto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String name;
    private String login;
    private String password;
    private String confirmPassword;
    private String birthdate;
    private LocalDate date;
}
