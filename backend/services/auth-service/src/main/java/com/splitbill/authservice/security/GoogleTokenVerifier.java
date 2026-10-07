package com.splitbill.authservice.security;

import com.splitbill.authservice.exception.AuthenticationFailureException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    private final JwtDecoder googleJwtDecoder;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public GoogleTokenVerifier(@Qualifier("googleJwtDecoder") JwtDecoder googleJwtDecoder) {
        this.googleJwtDecoder = googleJwtDecoder;
    }

    public GoogleUserInfo verify(String credential) {

        if (credential == null || credential.isBlank()) {
            throw new AuthenticationFailureException("Google credential is missing");
        }

        try {

            Jwt jwt = googleJwtDecoder.decode(credential);

            if (jwt.getAudience() == null || !jwt.getAudience()
                    .contains(googleClientId)) {

                throw new AuthenticationFailureException("Invalid Google client ID");
            }

            String subject = jwt.getSubject();

            String email = jwt.getClaimAsString("email");

            Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

            String name = jwt.getClaimAsString("name");

            if (subject == null || subject.isBlank()) {
                throw new AuthenticationFailureException("Google account subject is missing");
            }

            if (email == null || email.isBlank()) {
                throw new AuthenticationFailureException("Google account email is missing");
            }

            if (!Boolean.TRUE.equals(emailVerified)) {
                throw new AuthenticationFailureException("Google email is not verified");
            }

            return new GoogleUserInfo(subject, email, name);

        } catch (JwtException exception) {

            exception.printStackTrace();

            throw new AuthenticationFailureException("Invalid or expired Google credential: " + exception.getMessage());
        }
    }

    public record GoogleUserInfo(String subject, String email, String name) {
    }
}