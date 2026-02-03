package com.spring.lica.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @GetMapping("/user/login")
    public String loginPage() {
        return "user-login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "user-signup";
    }

    @GetMapping("/mypage")
    public String myPage() {
        return "mypage";
    }
}
