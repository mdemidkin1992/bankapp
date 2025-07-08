package ru.mdemidkin.cash.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.dto.CashRequest;
import ru.mdemidkin.cash.service.CashService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/user/{login}/сash")
    public Mono<ResponseEntity<String>> editCashBalance(@PathVariable String login,
                                                        @ModelAttribute CashRequest cashRequest,
                                                        ServerWebExchange exchange) {
        return cashService.updateCashBalance(login, cashRequest);
    }
}
