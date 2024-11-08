package org.example.authenticationservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotificationDTO {
    private String toEmail;
    private String fromEmail;
    private String subject;
    private String body;
}
