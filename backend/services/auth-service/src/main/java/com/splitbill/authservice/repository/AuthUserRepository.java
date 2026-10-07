package com.splitbill.authservice.repository;

import com.splitbill.authservice.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<AuthUser> findByProviderAndProviderSubject(AuthUser.Provider provider, String providerSubject);
}