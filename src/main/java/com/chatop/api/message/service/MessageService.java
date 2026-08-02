package com.chatop.api.message.service;

import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.entity.MessageEntity;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.entity.RentalEntity;
import com.chatop.api.rental.service.RentalService;
import com.chatop.api.user.entity.UserEntity;
import com.chatop.api.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RentalService rentalService;
    private final UserService userService;

    public MessageService(
            MessageRepository messageRepository,
            RentalService rentalService,
            UserService userService
    ) {
        this.messageRepository = messageRepository;
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @Transactional
    public void create(CreateMessageRequest request, String authenticatedEmail) {
        RentalEntity rental = rentalService.findById(request.rentalId());
        UserEntity author = userService.findByEmail(authenticatedEmail);
        messageRepository.save(new MessageEntity(rental, author, request.message().trim()));
    }
}
