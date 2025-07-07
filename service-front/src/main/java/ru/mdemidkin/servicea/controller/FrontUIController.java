package ru.mdemidkin.servicea.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mdemidkin.libdto.AccountDto;
import ru.mdemidkin.libdto.MainPageRequest;
import ru.mdemidkin.libdto.UserDto;

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

    @PostMapping("/main")
    public String renderMainPage(@RequestBody MainPageRequest request, Model model) {
        model.addAttribute("login", request.getLogin());
        model.addAttribute("name", request.getName());
        model.addAttribute("birthdate", request.getBirthdate());
        model.addAttribute("accounts", request.getAccounts());
        model.addAttribute("users", request.getUsers());
        model.addAttribute("currency", request.getCurrency());
        model.addAttribute("passwordErrors", request.getPasswordErrors());
        model.addAttribute("userAccountsErrors", request.getUserAccountsErrors());
        model.addAttribute("cashErrors", request.getCashErrors());
        model.addAttribute("transferErrors", request.getTransferErrors());
        model.addAttribute("transferOtherErrors", request.getTransferOtherErrors());
        return "main";
    }
}
