package com.oracle.truffle.api.vm;

import java.io.IOException;

/**
 * Indicates that the provided source was incomplete and requires further text to be executed.
 * 
 * @since 0.8 or earlier
 */
@SuppressWarnings("serial")
public class IncompleteSourceException extends IOException {
    /** @since 0.8 or earlier */
    public IncompleteSourceException() {
        super();
    }

    /** @since 0.8 or earlier */
    public IncompleteSourceException(Throwable cause) {
        super(cause);
    }

}
