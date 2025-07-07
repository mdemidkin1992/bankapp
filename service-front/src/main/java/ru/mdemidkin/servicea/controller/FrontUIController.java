package ru.mdemidkin.servicea.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping
public class FrontUIController {

    @GetMapping("/signup")
    public String getSignupHtml() {
        return "signup";
    }

    @PostMapping("/signup")
    public String getSignupHtmlWithError(@RequestParam String name,
                                         @RequestParam String login,
                                         @RequestParam String password,
                                         @RequestParam LocalDate birthdate,
                                         @RequestParam List<String> errors,
                                         Model model) {
        model.addAttribute("name", name);
        model.addAttribute("login", login);
        model.addAttribute("password", password);
        model.addAttribute("birthdate", birthdate);
        model.addAttribute("errors", errors);
        return "signup";
    }

    @GetMapping("/main")
    public String getMainHtml() {
        return "main";
    }
}
