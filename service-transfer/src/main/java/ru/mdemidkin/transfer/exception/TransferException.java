package ru.mdemidkin.transfer.exception;

import lombok.Getter;

@Getter
public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}
