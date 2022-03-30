package io.leego.example.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Leego Yih
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {
    UNKNOWN(0),
    MALE(1),
    FEMALE(2);

    private final int code;

    static final Map<Integer, GenderEnum> map = Arrays.stream(values())
            .collect(Collectors.toMap(GenderEnum::getCode, Function.identity()));

    public static GenderEnum get(Integer code) {
        return map.get(code);
    }
}
