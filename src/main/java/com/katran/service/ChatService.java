package com.katran.service;

import com.katran.model.Chat;
import com.katran.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    public Chat saveMessage(Chat chat) {
        return chatRepository.save(chat);
    }

    public List<Chat> getLastMessages(int limit) {
        return chatRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    }
}
