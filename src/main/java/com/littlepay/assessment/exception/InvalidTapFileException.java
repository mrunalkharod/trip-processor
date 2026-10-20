package com.littlepay.assessment.exception;

public class InvalidTapFileException extends RuntimeException {

    public InvalidTapFileException(long lineNumber, String problem) {
        super("Line " + lineNumber + ": " + problem);
    }

    public InvalidTapFileException(String message) {
        super(message);
    }
}
