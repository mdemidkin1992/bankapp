package ru.mdemidkin.servicea.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mdemidkin.servicea.client.ServiceBClient;

@Controller
@RequestMapping("/test")
public class TestController {

    @Value("${test-property}")
    private String testProperty;

//    private final ServiceBClient client;
//
//    public TestController(ServiceBClient client) {
//        this.client = client;
//    }

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(testProperty);
    }
}
