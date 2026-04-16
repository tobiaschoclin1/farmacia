package com.tobias.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String splash() {
        return "splash";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "index";
    }

    @GetMapping("/productos")
    public String productos() {
        return "productos";
    }

    @GetMapping("/entradas")
    public String entradas() {
        return "entradas";
    }

    @GetMapping("/salidas")
    public String salidas() {
        return "salidas";
    }

    @GetMapping("/stock")
    public String stock() {
        return "stock";
    }

    @GetMapping("/vencimientos")
    public String vencimientos() {
        return "vencimientos";
    }
}
