package com.lmp.demoGoogleLogin.repository;

import com.lmp.demoGoogleLogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String username);
}
