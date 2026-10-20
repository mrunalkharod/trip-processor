package com.littlepay.assessment.exception;


public class TapFileNotFoundException extends RuntimeException {

    public TapFileNotFoundException(Long id) {
        super("Tap file not found with id: " + id);
    }
}
