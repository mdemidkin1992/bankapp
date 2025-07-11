package ru.mdemidkin.libdto.account;

import lombok.Getter;

@Getter
public enum Currency {

    RUB("Рубль"),
    CNY("Юань"),
    USD("Доллар");

    private final String title;

    Currency(String title) {
        this.title = title;
    }

}
