package com.lmp.demoGoogleLogin.repository;

import com.lmp.demoGoogleLogin.entity.OauthToken;
import com.lmp.demoGoogleLogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface OauthTokenRepository extends JpaRepository<OauthToken, String> {
}