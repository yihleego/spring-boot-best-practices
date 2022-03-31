package io.leego.example.util;

/**
 * @author Leego Yih
 */
public class ServiceException extends RuntimeException {
    protected Integer code;
    protected Object[] args;

    /**
     * Constructs a new service exception.
     */
    public ServiceException() {
        super("Oops! An error has occurred.");
    }

    /**
     * Constructs a new service exception with cause.
     *
     * @param cause the cause.
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new service exception with specified detail message.
     *
     * @param message the detail message.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new service exception with specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new service exception with error code.
     *
     * @param code the error code.
     */
    public ServiceException(Integer code) {
        this.code = code;
    }

    /**
     * Constructs a new service exception with error code and cause.
     *
     * @param code  the error code.
     * @param cause the cause.
     */
    public ServiceException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * Constructs a new service exception with error code and specified detail message.
     *
     * @param code    the error code.
     * @param message the detail message.
     */
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructs a new service exception with error code, specified detail message and cause.
     *
     * @param code    the error code.
     * @param message the detail message.
     * @param cause   the cause.
     */
    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Constructs a new service exception with error.
     *
     * @param error the error.
     */
    public ServiceException(Error error) {
        this(error.getCode(), error.getMessage());
    }

    /**
     * Constructs a new service exception with error and cause.
     *
     * @param error the error.
     * @param cause the cause.
     */
    public ServiceException(Error error, Throwable cause) {
        this(error.getCode(), error.getMessage(), cause);
    }

    /**
     * Constructs a new service exception with specified detail message and args.
     *
     * @param message the detail message.
     * @param args    the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    /**
     * Constructs a new service exception with specified detail message, cause and args.
     *
     * @param message the detail message.
     * @param cause   the cause.
     * @param args    the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(String message, Throwable cause, Object... args) {
        super(message, cause);
        this.args = args;
    }

    /**
     * Constructs a new service exception with error code, specified detail message and args.
     *
     * @param code    the error code.
     * @param message the detail message.
     * @param args    the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(Integer code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    /**
     * Constructs a new service exception with error code, specified detail message, cause and args.
     *
     * @param code    the error code.
     * @param message the detail message.
     * @param cause   the cause.
     * @param args    the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(Integer code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    /**
     * Constructs a new service exception with error and args.
     *
     * @param error the error.
     * @param args  the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(Error error, Object... args) {
        this(error.getCode(), error.getMessage());
        this.args = args;
    }

    /**
     * Constructs a new service exception with error, cause and args.
     *
     * @param error the error.
     * @param cause the cause.
     * @param args  the array of arguments that will be filled in for params within the message.
     */
    public ServiceException(Error error, Throwable cause, Object... args) {
        this(error.getCode(), error.getMessage(), cause);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}