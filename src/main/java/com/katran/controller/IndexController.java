package com.katran.controller;

import com.katran.model.User;
import com.katran.service.PageVisitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Autowired
    PageVisitsService pageVisitsService;


    @GetMapping("/index")
    public String index(Model model) {
        pageVisitsService.incrementPageVisit("index");
        model.addAttribute("user", new User());
        return "index";
    }













}
