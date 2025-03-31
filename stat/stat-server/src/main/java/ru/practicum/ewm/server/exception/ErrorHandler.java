package ru.practicum.ewm.server.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({BadRequestException.class, MethodArgumentNotValidException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerBadRequestException(final BadRequestException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        log.error("Ошибка: 400 BAD_REQUEST - {}", stackTrace);
        return new ApiError("Запрос составлен некорректно", e.getMessage(),
                HttpStatus.BAD_REQUEST.name(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerException(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        log.error("Ошибка: 500 INTERNAL_SERVER_ERROR - {}", stackTrace);
        return new ApiError("Неизвестная ошибка", e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(), LocalDateTime.now());
    }
}