package ru.mdemidkin.libdto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String name;
    private String login;
    private String password;
    private String birthdate;
    private List<String> errors;
}
