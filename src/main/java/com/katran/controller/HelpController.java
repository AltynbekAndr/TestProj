package com.katran.controller;

import com.katran.model.User;
import com.katran.service.PageVisitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelpController {

    @Autowired
    PageVisitsService pageVisitsService;

    @GetMapping("/help")
    public String showHelpForm(Model model) {
        pageVisitsService.incrementPageVisit("help");
        model.addAttribute("user", new User());
        return "help";
    }
}
