package org.example.authenticationservice.repositories;

import org.example.authenticationservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    @Override
    public Session save(Session session);

    public Optional<Session> findByTokenAndUserId(String token, Long userId);
}
