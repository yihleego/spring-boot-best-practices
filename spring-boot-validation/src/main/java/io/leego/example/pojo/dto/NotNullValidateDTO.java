package io.leego.example.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Leego Yih
 */
@Data
public class NotNullValidateDTO {
    @NotNull(message = "无效数据")
    private String value;
}
