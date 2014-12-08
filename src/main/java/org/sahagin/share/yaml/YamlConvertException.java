package org.sahagin.share.yaml;

public class YamlConvertException extends Exception {

    private static final long serialVersionUID = 1L;

    public YamlConvertException(String message) {
        super(message);
    }

    public YamlConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public YamlConvertException(Throwable cause) {
        super(cause);
    }

}
