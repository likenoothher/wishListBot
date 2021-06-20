package com.aziarets.vividapp.controllers;

import com.aziarets.vividapp.dao.BotUserRepo;
import com.aziarets.vividapp.model.BotUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private BotUserRepo botUserRepo;

    @RequestMapping("/1")
    public String findBotUserById(Model model){
       BotUser botUser = botUserRepo.getById(1);
       model.addAttribute("user", botUser);
       return "main";
    }
}
