package org.example.rainzubinringcounter.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
    INCORRECT_FILE_FORMAT("Incorrect file format"),
    INVALID_FILE_FORMAT("Invalid file format");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

}
