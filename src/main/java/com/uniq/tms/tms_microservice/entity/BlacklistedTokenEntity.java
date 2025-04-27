package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", columnDefinition = "TEXT", nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "logged_out_at", nullable = false)
    private LocalDateTime loggedOutAt;

    @PrePersist
    protected void onLogout() {
        loggedOutAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserEntity getUser() { return userEntity; }
    public void setUser(UserEntity userEntity) { this.userEntity = userEntity; }
    public LocalDateTime getLoggedOutAt() { return loggedOutAt; }
    public void setLoggedOutAt(LocalDateTime loggedOutAt) { this.loggedOutAt = loggedOutAt; }
}
