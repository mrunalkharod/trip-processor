package com.littlepay.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(InvalidTapFileException.class)
    public ProblemDetail handleInvalidTapFile(InvalidTapFileException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Tap File", ex.getMessage());
    }

    @ExceptionHandler(TapFileNotFoundException.class)
    public ProblemDetail handleTapFileNotFound(TapFileNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Tap File Not Found", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
