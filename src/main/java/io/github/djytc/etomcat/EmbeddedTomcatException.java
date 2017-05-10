package io.github.djytc.etomcat;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class EmbeddedTomcatException extends RuntimeException {
    public EmbeddedTomcatException(String message) {
        super(message);
    }

    public EmbeddedTomcatException(String message, Throwable cause) {
        super(message, cause);
    }
}
