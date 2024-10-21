package org.example.authenticationservice.services;

public interface IAuthService {
    boolean signUp(String email, String password);
    String login(String email, String password);
    Boolean validateToken(Long userId, String token);
}
