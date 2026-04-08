package com.tobias.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
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

    @GetMapping("/import-export")
    public String importExport() {
        return "import-export";
    }
}
