package io.leego.example.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Leego Yih
 */
@Getter
@AllArgsConstructor
public enum Status {
    DISABLED(0, "status.disabled"),
    ENABLED(1, "status.enabled");

    private final int code;
    private final String remark;
}
