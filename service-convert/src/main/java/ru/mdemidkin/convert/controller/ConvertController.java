package ru.mdemidkin.convert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;
import ru.mdemidkin.convert.service.ConvertService;
import ru.mdemidkin.libdto.transfer.TransferRequest;

import java.math.BigDecimal;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class ConvertController {

    private final ConvertService convertService;

    @ResponseBody
    @PostMapping("/api/convert")
    public Mono<BigDecimal> convertAmount(@RequestBody TransferRequest transferRequest) {
        return convertService.convertAmount(
                transferRequest.getFromCurrency(),
                transferRequest.getToCurrency(),
                transferRequest.getValue());
    }
}
