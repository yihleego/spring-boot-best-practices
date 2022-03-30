package io.leego.example.controller;

import io.leego.example.pojo.dto.GenderValidateDTO;
import io.leego.example.pojo.dto.GroupValidateDTO;
import io.leego.example.pojo.dto.NotNullValidateDTO;
import io.leego.example.pojo.dto.PhoneValidateDTO;
import io.leego.example.pojo.dto.UsernameValidateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Leego Yih
 */
@RestController
public class ValidationController {
    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

    @GetMapping("testNotNull")
    public boolean testNotNull(@Validated NotNullValidateDTO dto) {
        logger.debug("Valid value: {}", dto.getValue());
        return true;
    }

    @GetMapping("testPhone")
    public boolean testPhone(@Validated PhoneValidateDTO dto) {
        logger.debug("Valid phone: {}", dto.getPhone());
        return true;
    }

    @GetMapping("testUsername")
    public boolean testUsername(@Validated UsernameValidateDTO dto) {
        logger.debug("Valid username: {}", dto.getUsername());
        return true;
    }

    @GetMapping("testGender")
    public boolean testGender(@Validated GenderValidateDTO dto) {
        logger.debug("Valid gender: {}", dto.getGender());
        return true;
    }

    @GetMapping("testGroup1")
    public boolean testGroup1(@Validated(GroupValidateDTO.G1.class) GroupValidateDTO dto) {
        logger.debug("Valid value1: {}, value2:{}", dto.getValue1(), dto.getValue2());
        return true;
    }

    @GetMapping("testGroup2")
    public boolean testGroup2(@Validated(GroupValidateDTO.G2.class) GroupValidateDTO dto) {
        logger.debug("Valid value1: {}, value2:{}", dto.getValue1(), dto.getValue2());
        return true;
    }

}
