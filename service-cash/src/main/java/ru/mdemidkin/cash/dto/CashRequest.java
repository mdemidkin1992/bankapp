package ru.mdemidkin.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mdemidkin.libdto.CashAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRequest {
    private String currency;
    private String value;
    private CashAction action;
}
