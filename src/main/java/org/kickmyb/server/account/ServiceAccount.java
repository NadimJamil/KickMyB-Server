package org.kickmyb.server.account;

import org.kickmyb.transfer.SignupRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

// extends UserDetailsService which is one of the Spring Security entry points
public interface ServiceAccount extends UserDetailsService {

    class UsernameTooShort extends Exception {
        public UsernameTooShort(String message) {
            super(message);
        }
    }
    class UsernameAlreadyTaken extends Exception {
        public UsernameAlreadyTaken(String message) {
            super(message);
        }
    }
    class PasswordTooShort extends Exception {
        public PasswordTooShort(String message) {
            super(message);
        }
    }

    void signup(SignupRequest req) throws BadCredentialsException, UsernameTooShort, PasswordTooShort, UsernameAlreadyTaken;

}
