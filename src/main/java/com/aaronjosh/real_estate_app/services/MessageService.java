package com.aaronjosh.real_estate_app.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.models.ConversationEntity;
import com.aaronjosh.real_estate_app.models.MessageEntity;
import com.aaronjosh.real_estate_app.models.ParticipantEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.ConversationRepository;
import com.aaronjosh.real_estate_app.util.BookingMessageTemplate;

@Service
public class MessageService {
    @Autowired
    private BookingMessageTemplate bookingMessageTemplate;

    @Autowired
    private ConversationRepository conversationRepo;

    public void createBookingRequestMessage(UserEntity user, BookingReqDto bookingInfo, PropertyEntity property) {
        // Create conversation
        ConversationEntity conversation = new ConversationEntity();

        // Create participants
        ParticipantEntity propertyOwner = new ParticipantEntity();
        propertyOwner.setWhoJoined(property.getHost());

        ParticipantEntity renter = new ParticipantEntity();
        renter.setWhoJoined(user);

        // Create initial message
        String messageTemplate = bookingMessageTemplate.MessageTemplate(bookingInfo, property);
        MessageEntity message = new MessageEntity();
        message.setMesssages(messageTemplate);
        message.setFrom(user);

        conversation.setParticipants(List.of(propertyOwner, renter));
        conversation.setMessages(List.of(message));
        propertyOwner.setConversation(conversation);
        renter.setConversation(conversation);

        conversationRepo.save(conversation);
    }

}
