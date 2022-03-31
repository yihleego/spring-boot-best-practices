package io.leego.example.enumeration;

import io.leego.example.constant.Messages;
import io.leego.example.util.Code;
import io.leego.example.util.Error;
import lombok.Getter;

/**
 * @author Leego Yih
 */
@Getter
public enum ErrorCode implements Error, Code {
    ERROR(1, 0, Messages.ERROR),
    ;

    private final Integer code;
    private final String message;
    private final int type = 0;
    private final int tag;
    private final int index;

    ErrorCode(int tag, int index, String message) {
        this.tag = tag;
        this.index = index;
        this.code = toCode();
        this.message = message;
    }
}
