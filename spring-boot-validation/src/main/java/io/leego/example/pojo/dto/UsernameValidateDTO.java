package io.leego.example.pojo.dto;

import io.leego.example.validation.Username;
import lombok.Data;

/**
 * @author Leego Yih
 */
@Data
public class UsernameValidateDTO {
    @Username(message = "用户名格式不正确")
    private String username;
}
