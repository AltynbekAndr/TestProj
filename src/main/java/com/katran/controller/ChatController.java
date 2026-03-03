package com.katran.controller;

import com.katran.model.Chat;
import com.katran.model.User;
import com.katran.repository.ChatRepository;
import com.katran.service.ChatService;
import com.katran.service.PageVisitsService;
import com.katran.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @Autowired
    PageVisitsService pageVisitsService;

    @GetMapping
    public String chat(Model model,HttpSession session) {
        pageVisitsService.incrementPageVisit("chat");

        List<Chat> lastMessages = chatService.getLastMessages(100);
        model.addAttribute("lastMessages", lastMessages);
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "chat"; // Название вашего шаблона
    }


    @ResponseBody
    @PostMapping("/send-message")
    public String sendMessage(
            @RequestBody Map<String, String> requestBody,
            Model model,
            HttpSession session) {
        pageVisitsService.incrementPageVisit("chat");
        String message = requestBody.get("message");
        User user = (User)session.getAttribute("user");

        if (user != null) {
            model.addAttribute("user",user);
            long userId = user.getId();
            Chat chat = new Chat();
            chat.setUserId(userId);
            chat.setFull_name(user.getFull_name());
            chat.setMessage(message);
            chatService.saveMessage(chat);
            return "{\"code\":0,\"message\":\"ok\"}";
        }
        return "{\"code\":303,\"message\":\"redirect\"}";

    }










}
