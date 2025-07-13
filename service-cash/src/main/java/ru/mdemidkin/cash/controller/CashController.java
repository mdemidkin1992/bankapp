package ru.mdemidkin.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.dto.CashRequest;
import ru.mdemidkin.cash.service.CashService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/user/{login}/сash")
    public Mono<ResponseEntity<Void>> editCashBalance(@PathVariable String login,
                                                      @ModelAttribute CashRequest cashRequest) {
        return cashService.updateCashBalance(login, cashRequest);
    }
}
