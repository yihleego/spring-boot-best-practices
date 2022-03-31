package io.leego.example.controller;

import io.leego.example.constant.Messages;
import io.leego.example.enumeration.ErrorCode;
import io.leego.example.enumeration.Status;
import io.leego.example.util.Result;
import io.leego.example.util.ServiceException;
import lombok.Data;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Leego Yih
 */
@RestController
public class MessageController {
    private final MessageSource messageSource;

    public MessageController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("testSimpleMessage")
    public String testSimpleMessage() {
        return messageSource.getMessage(Messages.SIMPLE, null, null, LocaleContextHolder.getLocale());
    }

    @GetMapping("testArgsMessage")
    public String testArgsMessage() {
        return messageSource.getMessage(Messages.ARGS, new String[]{"Paul"}, null, LocaleContextHolder.getLocale());
    }

    @GetMapping("testDefaultMessage")
    public String testDefaultMessage() {
        // 'missing' is not configured
        return messageSource.getMessage(Messages.MISSING, null, "Message is missing", LocaleContextHolder.getLocale());
    }

    @GetMapping("testDefaultMessage2")
    public String testDefaultMessage2() {
        String message = messageSource.getMessage(Messages.MISSING, null, null, LocaleContextHolder.getLocale());
        if (message != null) {
            return message;
        }
        return messageSource.getMessage(Messages.DEFAULT, null, null, LocaleContextHolder.getLocale());
    }

    @GetMapping("testResultMessage")
    public Result<Void> testResultMessage() {
        return Result.buildFailure(Messages.ERROR);
    }

    @GetMapping("testResultError")
    public Result<Void> testResultError() {
        return Result.buildFailure(ErrorCode.ERROR);
    }

    @GetMapping("testException")
    public boolean testException(@RequestParam(defaultValue = "true") boolean flag) {
        if (flag) {
            throw new RuntimeException(Messages.ERROR);
        }
        return true;
    }

    @GetMapping("testException2")
    public boolean testException2(@RequestParam(defaultValue = "true") boolean flag) {
        if (flag) {
            throw new ServiceException(ErrorCode.ERROR);
        }
        return true;
    }

    @GetMapping("testInvalid")
    public boolean testInvalid(@Validated Param param) {
        return true;
    }

    @GetMapping("testEnum")
    public Map<Integer, String> testEnum() {
        Map<Integer, String> map = new HashMap<>();
        for (Status status : Status.values()) {
            map.put(status.getCode(), messageSource.getMessage(status.getRemark(), null, null, LocaleContextHolder.getLocale()));
        }
        return map;
    }

    @Data
    public class Param {
        @NotNull(message = "key.invalid")
        private String key;
        @NotNull(message = "{value.invalid}")
        private String value;
    }
}
