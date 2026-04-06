package com.osucollector.api.pack;

public class NoPackAvailableException extends RuntimeException {
    public NoPackAvailableException() {
        super("No pack available, please wait for the next one");
    }
}