package io.leego.example.pojo.dto;

import io.leego.example.validation.Phone;
import lombok.Data;

/**
 * @author Leego Yih
 */
@Data
public class PhoneValidateDTO {
    @Phone(message = "手机号码格式不正确")
    //@Pattern(regexp = "^(1[3-9])\\d{9}$", message = "手机号码格式不正确")
    private String phone;
}
