package ru.mdemidkin.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.service.CashService;
import ru.mdemidkin.libdto.cash.CashRequest;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/user/{login}/cash")
    public Mono<ResponseEntity<Void>> editCashBalance(@PathVariable String login,
                                                      @ModelAttribute CashRequest cashRequest) {
        return cashService.updateCashBalance(login, cashRequest);
    }
}
