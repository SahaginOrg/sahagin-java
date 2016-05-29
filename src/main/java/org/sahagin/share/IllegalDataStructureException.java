package org.sahagin.share;


// Thrown when data structure (srcTree, runResult, etc) is illegal.
// This exception occurs mainly because some internal data such as YAML data are invalid,
// and most users don't see this exception as long as sahagin or
// some sahagin extension program have any problem.
public class IllegalDataStructureException extends Exception {

    private static final long serialVersionUID = 1L;

    public IllegalDataStructureException(String message) {
        super(message);
    }

    public IllegalDataStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDataStructureException(Throwable cause) {
        super(cause);
    }
}
