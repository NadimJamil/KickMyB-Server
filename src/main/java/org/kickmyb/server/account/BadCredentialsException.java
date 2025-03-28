package org.kickmyb.server.account;

public class BadCredentialsException extends Exception {
    public BadCredentialsException(String message) {
        super(message);
    }
}
