package org.sahagin.share;

// Thrown when test scripts written by user have problems.
// (Maybe users should correct the test script mistakes)
public class IllegalTestScriptException extends Exception {

    private static final long serialVersionUID = 1L;

    public IllegalTestScriptException(String message) {
        super(message);
    }

    public IllegalTestScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTestScriptException(Throwable cause) {
        super(cause);
    }

}
