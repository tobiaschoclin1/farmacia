package com.tobias.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/usuario")
    public String usuario() {
        return "usuario";
    }
}
