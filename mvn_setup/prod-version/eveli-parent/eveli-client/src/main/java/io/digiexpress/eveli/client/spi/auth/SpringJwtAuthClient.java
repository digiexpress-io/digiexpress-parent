package io.digiexpress.eveli.client.spi.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.digiexpress.eveli.client.api.ImmutableUser;
import io.digiexpress.eveli.client.api.ImmutableUserPrincipal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringJwtAuthClient implements AuthClient {

  @Override
  public User getUser() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(!authentication.isAuthenticated()) {
      return ImmutableUser.builder()
          .isAuthenticated(false)
          .principal(ImmutableUserPrincipal.builder()
              .username("UNAUTHENTICATED")
              .email("")
              .build())
          .build();
    }
    
    final Jwt token = (Jwt) authentication.getCredentials();
    return ImmutableUser.builder()
        .isAuthenticated(true)
        .principal(ImmutableUserPrincipal.builder()
            .username(authentication.getName())
            .email(getEmail(token))
            .roles(authentication.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()))
            .build())
        .build();
  }

  @Override
  public Liveness getLiveness() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    final Jwt token = (Jwt) authentication.getCredentials();
    
    final var now = LocalDateTime.now();
    final var then = LocalDateTime.ofInstant(token.getExpiresAt(), ZoneId.systemDefault());
    return ImmutableLiveness.builder()
        .issuedAtTime(token.getIssuedAt().toEpochMilli())
        .expiresIn(Duration.between(now, then).toSeconds())
        .build();
  
  }
  
  private String getEmail(Jwt principal) {
    String email = "";
    if (principal != null) {
      email = principal.getClaimAsString("email");
    }
    return email;
  }
}
