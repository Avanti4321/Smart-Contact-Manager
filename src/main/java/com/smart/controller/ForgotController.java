package com.smart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForgotController {
    //email id form open handler
    @RequestMapping("/forgot")
    public String openEmailForm(){
       return "forgot_email_form";
    }
}
