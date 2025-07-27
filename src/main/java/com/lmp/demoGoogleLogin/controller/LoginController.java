package com.lmp.demoGoogleLogin.controller;

import com.lmp.demoGoogleLogin.helper.LoginHelper;
import com.lmp.demoGoogleLogin.model.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
    @Autowired
    LoginHelper loginHelper;

    @GetMapping("/test")
    public String test() {
        return "¡Hola mundo!";
    }

    @GetMapping("/grant-code")
    public LoginResponseDTO grantCode(
            @RequestParam("code") String code,
            @RequestParam("scope") String scope,
            @RequestParam("authuser") String authUser,
            @RequestParam("prompt") String prompt) {
        return loginHelper.processGrantCode(code);
    }
}
