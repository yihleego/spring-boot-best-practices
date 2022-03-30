package io.leego.example.pojo.dto;

import io.leego.example.validation.Gender;
import lombok.Data;

/**
 * @author Leego Yih
 */
@Data
public class GenderValidateDTO {
    @Gender(nullable = true, message = "性别格式不正确")
    private Integer gender;
}
