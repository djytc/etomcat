package io.github.djytc.etomcat;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class ETomcatException extends RuntimeException {
    public ETomcatException(String message) {
        super(message);
    }

    public ETomcatException(String message, Throwable cause) {
        super(message, cause);
    }
}
