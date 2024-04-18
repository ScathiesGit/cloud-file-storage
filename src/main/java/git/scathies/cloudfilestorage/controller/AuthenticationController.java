package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @GetMapping
    public String loginForm() {
        return "login";
    }

    @GetMapping("/reg")
    public String registrationForm() {
        return "registration";
    }

    @PostMapping("/reg")
    public String processRegistration(User user) {
        userService.createUser(user);
        return "login";
    }
}
