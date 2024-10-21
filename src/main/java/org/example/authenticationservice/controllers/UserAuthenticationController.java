package org.example.authenticationservice.controllers;

import org.example.authenticationservice.dto.*;
import org.example.authenticationservice.models.UserAuthenticationStatus;
import org.example.authenticationservice.services.IAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserAuthenticationController {
    private final IAuthService userAuthenticationService;

    public UserAuthenticationController(IAuthService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @PostMapping("/sign_up")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        SignUpResponseDTO responseDTO = new SignUpResponseDTO();

        try {
            if(userAuthenticationService.signUp(signUpRequestDTO.getEmail(), signUpRequestDTO.getPassword())) {
                responseDTO.setStatus(UserAuthenticationStatus.SUCCESS);
            } else {
                responseDTO.setStatus(UserAuthenticationStatus.FAILURE);
            }

            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            responseDTO.setStatus(UserAuthenticationStatus.FAILURE);
            return new ResponseEntity<>(responseDTO, HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO responseDTO = new LoginResponseDTO();

        try{
            String authToken = userAuthenticationService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            responseDTO.setUserAuthenticationStatus(UserAuthenticationStatus.SUCCESS);
            responseDTO.setAuthToken(authToken);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.SET_COOKIE, authToken);

            return new ResponseEntity<>(responseDTO, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            responseDTO.setUserAuthenticationStatus(UserAuthenticationStatus.FAILURE);
            return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestBody ValidateTokenRequestDTO validateTokenRequestDTO) {
        Boolean isTokenValid = userAuthenticationService.validateToken(validateTokenRequestDTO.getUserId(), validateTokenRequestDTO.getToken());
        return new ResponseEntity<>(isTokenValid, HttpStatus.OK);
    }
}
