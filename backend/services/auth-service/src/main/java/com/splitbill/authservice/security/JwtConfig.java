package com.splitbill.authservice.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Bean
    public KeyPair jwtKeyPair() {

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            return generator.generateKeyPair();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate JWT RSA key pair", e);
        }
    }

    @Bean
    public RSAKey jwtRsaKey(KeyPair jwtKeyPair) {

        return new RSAKey.Builder((RSAPublicKey) jwtKeyPair.getPublic()).privateKey(jwtKeyPair.getPrivate())
                .keyID("splitbill-auth-key")
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey jwtRsaKey) {

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwtRsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder(KeyPair jwtKeyPair) {

        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic())
                .build();
    }

    @Bean
    public JwtDecoder googleJwtDecoder() {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation("https://accounts.google.com")
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {

            if (jwt.getAudience()
                    .contains(googleClientId)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Google token audience", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));

        return decoder;
    }
}