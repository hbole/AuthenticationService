package org.example.authenticationservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "sessions")
public class Session extends BaseModel {
    @Enumerated(EnumType.ORDINAL)
    private SessionState sessionState;
    private String token;

    @ManyToOne
    private User user;
}
