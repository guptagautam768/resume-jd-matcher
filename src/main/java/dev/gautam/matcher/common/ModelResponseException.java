package dev.gautam.matcher.common;

public class ModelResponseException extends RuntimeException {

    public ModelResponseException(String message) {
        super(message);
    }

    public ModelResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
