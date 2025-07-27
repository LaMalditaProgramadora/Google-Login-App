package com.lmp.demoGoogleLogin.config;

import com.lmp.demoGoogleLogin.entity.OauthToken;
import com.lmp.demoGoogleLogin.entity.User;
import com.lmp.demoGoogleLogin.repository.OauthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BearerTokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    OauthTokenRepository tokenRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = isTokenValid(authentication.getName());

        if (user == null) {
            throw new UsernameNotFoundException("Invalid Token");
        }

        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());

    }

    private User isTokenValid(String token) {
        OauthToken oauthToken = tokenRepository.findById(token).orElse(null);

        if (oauthToken == null || oauthToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        return oauthToken.getUser();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(BearerTokenAuthentication.class);
    }
}
