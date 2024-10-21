package org.example.authenticationservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.authenticationservice.models.UserAuthenticationStatus;

@Getter
@Setter
public class SignUpResponseDTO {
    private UserAuthenticationStatus status;
}
