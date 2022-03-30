package io.leego.example.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Leego Yih
 */
@RestControllerAdvice
public class WebControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(WebControllerAdvice.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error("", e);
        return buildConstraintViolation(e.getBindingResult(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String handleBindException(BindException e) {
        logger.error("", e);
        return buildConstraintViolation(e.getBindingResult(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public String handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("", e);
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public String handleException(Exception e) {
        logger.error("", e);
        return e.getMessage();
    }

    private String buildConstraintViolation(BindingResult result, String defaultMessage) {
        if (result == null || result.getErrorCount() <= 0) {
            return defaultMessage;
        }
        return result.getAllErrors().get(0).getDefaultMessage();
    }
}
