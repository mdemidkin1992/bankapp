package ru.mdemidkin.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditAccountsRequest {
    private String name;
    private String birthdate;
    private List<String> account;
}
