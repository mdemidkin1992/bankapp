package ru.mdemidkin.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.transfer.TransferRequest;
import ru.mdemidkin.transfer.service.TransferService;

import java.math.BigDecimal;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/user/{login}/transfer")
    public Mono<ResponseEntity<Void>> editCashBalance(@PathVariable String login,
                                                      ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String fromCurrency = formData.getFirst("from_currency");
                    String toCurrency = formData.getFirst("to_currency");
                    String valueStr = formData.getFirst("value");
                    String toLogin = formData.getFirst("to_login");

                    BigDecimal value = new BigDecimal(valueStr);

                    TransferRequest transferRequest = TransferRequest.builder()
                            .fromCurrency(fromCurrency)
                            .toCurrency(toCurrency)
                            .value(value)
                            .toLogin(toLogin)
                            .build();

                    return transferService.processTransfer(login, transferRequest);
                });
    }
}
