package ru.mdemidkin.transfer.exception;

import lombok.Getter;

@Getter
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
