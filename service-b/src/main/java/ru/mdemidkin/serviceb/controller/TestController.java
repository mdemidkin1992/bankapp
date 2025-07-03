package ru.mdemidkin.serviceb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {

    @Value("${test-property}")
    private String testProperty;

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(testProperty);
    }


}
