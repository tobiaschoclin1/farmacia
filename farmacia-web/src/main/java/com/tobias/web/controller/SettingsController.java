package com.tobias.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/configuracion")
    public String configuracion() {
        return "configuracion";
    }
}
