package org.example.rainzubinringcounter.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
    INCORRECT_FILE_FORMAT("Incorrect file format"),
    INCORRECT_TABLE_HANDLER("The header of the table should be written as: ТАЙМ-КОД ПЕРСОНАЖ ТЕКСТ");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

}
