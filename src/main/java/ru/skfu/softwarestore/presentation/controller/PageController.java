package ru.skfu.softwarestore.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    @GetMapping("/") public String index() { return "index"; }
    @GetMapping("/catalog") public String catalog() { return "catalog"; }
    @GetMapping("/product/{id}") public String product(@PathVariable String id) { return "product"; }
    @GetMapping("/cart") public String cart() { return "cart"; }
    @GetMapping("/auth") public String auth() { return "auth"; }
    @GetMapping("/profile") public String profile() { return "profile"; }
    @GetMapping("/orders") public String orders() { return "orders"; }
    @GetMapping("/licenses") public String licenses() { return "licenses"; }
    @GetMapping("/admin") public String admin() { return "admin/dashboard"; }
}
