package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.dto.message.ChatHeadDto;
import com.aaronjosh.real_estate_app.models.ConversationEntity;
import com.aaronjosh.real_estate_app.models.MessageEntity;
import com.aaronjosh.real_estate_app.models.ParticipantEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.ConversationRepository;
import com.aaronjosh.real_estate_app.repositories.MessageRepository;
import com.aaronjosh.real_estate_app.util.BookingMessageTemplate;

@Service
public class MessageService {
    @Autowired
    private BookingMessageTemplate bookingMessageTemplate;

    @Autowired
    private ConversationRepository conversationRepo;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    public List<ChatHeadDto> getChatHeads() {
        UserEntity user = userService.getUserEntity();

        List<ConversationEntity> conversations = conversationRepo.findConversationsByParticipantId(user.getId());

        List<ChatHeadDto> dtos = new ArrayList<>();

        for (ConversationEntity convo : conversations) {
            ChatHeadDto dto = new ChatHeadDto();

            List<MessageEntity> messages = messageRepository.findLatestMessage(convo.getId());

            MessageEntity latestMessage = messages.stream().findFirst().orElse(null);

            if (latestMessage != null) {
                dto.setMessagePreview(latestMessage.getMesssages());
                dto.setDate(latestMessage.getCreatedAt());
            }

            dto.setChatName(convo.getConversationName());
            dto.setProfileLink(null);

            dtos.add(dto);
        }

        return dtos;
    }

    public void createBookingRequestMessage(UserEntity user, BookingReqDto bookingInfo, PropertyEntity property) {
        // TODO check if there was a existing message

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
        message.setConversation(conversation);

        conversation.setParticipants(List.of(propertyOwner, renter));
        conversation.setMessages(List.of(message));
        propertyOwner.setConversation(conversation);
        renter.setConversation(conversation);

        conversationRepo.save(conversation);
    }

}
