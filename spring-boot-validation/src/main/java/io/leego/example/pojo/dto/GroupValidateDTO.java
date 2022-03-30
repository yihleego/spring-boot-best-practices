package io.leego.example.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Leego Yih
 */
@Data
public class GroupValidateDTO {
    @NotNull(groups = G1.class, message = "Value1 不能为空")
    private String value1;
    @NotNull(groups = G2.class, message = "Value2 不能为空")
    private String value2;

    public interface G1 {}

    public interface G2 {}
}
