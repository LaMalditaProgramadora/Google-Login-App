package com.lmp.demoGoogleLogin.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lmp.demoGoogleLogin.entity.OauthToken;
import com.lmp.demoGoogleLogin.entity.User;
import com.lmp.demoGoogleLogin.model.LoginResponseDTO;
import com.lmp.demoGoogleLogin.repository.OauthTokenRepository;
import com.lmp.demoGoogleLogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Transactional
public class LoginHelper {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OauthTokenRepository tokenRepository;

    @Value("${google.client.id}")
    String clientId;
    @Value("${google.client.secret}")
    String clientSecret;
    @Value("${server.host}")
    String serverHost;

    public User registerUser(String firstName, String lastName, String email, String password) {
        User user = new User();
        user.setEnabled(true);
        user.setRole("USER");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        user = userRepository.save(user);
        return user;
    }

    private LoginResponseDTO saveTokenForUser(User user) {
        LoginResponseDTO dto = generateToken();
        OauthToken token = new OauthToken();
        token.setAccessToken(dto.getAccessToken());
        token.setRefreshToken(dto.getRefreshToken());
        token.setExpirationTime(dto.getExpirationTime());
        token.setUser(user);

        tokenRepository.save(token);
        return dto;
    }

    private LoginResponseDTO generateToken() {
        LoginResponseDTO res = new LoginResponseDTO();
        res.setAccessToken(UUID.randomUUID().toString());
        res.setRefreshToken(UUID.randomUUID().toString());
        res.setExpirationTime(LocalDateTime.now().plusHours(1));
        return res;
    }

    public LoginResponseDTO processGrantCode(String code) {
        String accessToken = getOauthAccessTokenGoogle(code);

        User googleUser = getProfileDetailsGoogle(accessToken);
        User user = userRepository.findByEmail(googleUser.getEmail());

        if (user == null) {
            user = registerUser(googleUser.getFirstName(), googleUser.getLastName(), googleUser.getEmail(), googleUser.getPassword());
        }

        return saveTokenForUser(user);
    }

    private User getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);

        User user = new User();
        user.setEmail(jsonObject.get("email").toString().replace("\"", ""));
        user.setFirstName(jsonObject.get("name").toString().replace("\"", ""));
        user.setLastName(jsonObject.get("given_name").toString().replace("\"", ""));
        user.setPassword(UUID.randomUUID().toString());

        return user;
    }

    private String getOauthAccessTokenGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = getMultiValueMapHttpEntity(code, httpHeaders);

        System.out.println("=== Google OAuth Token Request ===");
        System.out.println("Headers: " + requestEntity.getHeaders());
        System.out.println("Body: " + requestEntity.getBody());
        System.out.println("===================================");

        String url = "https://oauth2.googleapis.com/token";
        String response = restTemplate.postForObject(url, requestEntity, String.class);
        System.out.println("Google token response: " + response);
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);

        return jsonObject.get("access_token").toString().replace("\"", "");
    }

    private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String code, HttpHeaders httpHeaders) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", serverHost + "/grant-code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
        params.add("scope", "openid");
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);
        return requestEntity;
    }
}
