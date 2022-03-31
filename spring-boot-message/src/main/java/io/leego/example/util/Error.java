package io.leego.example.util;

/**
 * @author Leego Yih
 */
public interface Error {

    /** Returns error code. */
    Integer getCode();

    /** Returns error message. */
    String getMessage();

}