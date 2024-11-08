package org.example.authenticationservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.example.authenticationservice.clients.KafkaProducerClient;
import org.example.authenticationservice.dto.EmailNotificationDTO;
import org.example.authenticationservice.exceptions.UserAlreadyExistsException;
import org.example.authenticationservice.exceptions.UserNotFoundException;
import org.example.authenticationservice.exceptions.WrongPasswordException;
import org.example.authenticationservice.models.Session;
import org.example.authenticationservice.models.SessionState;
import org.example.authenticationservice.models.User;
import org.example.authenticationservice.repositories.SessionRepository;
import org.example.authenticationservice.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserAuthenticationService implements IAuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SessionRepository sessionRepository;
    private final SecretKey secretKey;
    private final KafkaProducerClient kafkaProducerClient;
    private final ObjectMapper objectMapper;

    public UserAuthenticationService(
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            SecretKey secretKey,
            SessionRepository sessionRepository,
            KafkaProducerClient kafkaProducerClient,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.secretKey = secretKey;
        this.sessionRepository = sessionRepository;
        this.kafkaProducerClient = kafkaProducerClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean signUp(String email, String password) throws UserAlreadyExistsException {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(newUser);

        //Send email
        try {
            EmailNotificationDTO emailNotificationDTO = new EmailNotificationDTO();
            emailNotificationDTO.setFromEmail("anuragbatch@gmail.com");
            emailNotificationDTO.setToEmail(email);
            emailNotificationDTO.setSubject("Welcome to Scaler");
            emailNotificationDTO.setBody("Hope you have a fun learning ahead!!");

            kafkaProducerClient.sendMessage("onboarding", objectMapper.writeValueAsString(emailNotificationDTO));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        return true;
    }

    @Override
    public String login(String email, String password) throws UserNotFoundException, WrongPasswordException {
        Optional<User> user = userRepository.findByEmail(email);
        User foundUser;

        if(user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        foundUser = user.get();

        if(!bCryptPasswordEncoder.matches(password, foundUser.getPassword())) {
            throw new WrongPasswordException("Wrong password");
        }

        //check the current time stamp and compare with session and then mark entry as
        //active or expired: TODO

        //JWT Token Generation
        Map<String, Object> claims = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        claims.put("iat", currentTime);
        claims.put("exp", currentTime + 2592000);
        claims.put("user_id", foundUser.getId());
        claims.put("issuer", "scaler");

        String jwtToken = Jwts.builder().claims(claims).signWith(this.secretKey).compact();

        Session session = new Session();
        session.setToken(jwtToken);
        session.setSessionState(SessionState.ACTIVE);
        session.setUser(foundUser);

        sessionRepository.save(session);
        return jwtToken;
    }

    @Override
    public Boolean validateToken(Long userId, String token) {
        Optional<Session> optionalSession = sessionRepository.findByTokenAndUserId(token, userId);

        if(optionalSession.isEmpty()) {
            return false;
        }

        JwtParser jwtParser = Jwts.parser().verifyWith(this.secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        long expiry = (long)claims.get("exp");
        long now = System.currentTimeMillis();

        if(now > expiry) {
            Session session = optionalSession.get();
            session.setSessionState(SessionState.EXPIRED);
            sessionRepository.save(session);
            return false;
        }
        return true;
    }
}
