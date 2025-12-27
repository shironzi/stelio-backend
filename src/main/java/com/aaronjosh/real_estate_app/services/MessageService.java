package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.dto.message.ChatHeadDto;
import com.aaronjosh.real_estate_app.dto.message.MessageResDto;
import com.aaronjosh.real_estate_app.dto.message.SendMessageDto;
import com.aaronjosh.real_estate_app.models.ConversationEntity;
import com.aaronjosh.real_estate_app.models.FileEntity;
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

    @Autowired
    private FileService fileService;

    public List<ChatHeadDto> getChatHeads() {
        UserEntity user = userService.getUserEntity();

        List<ConversationEntity> conversations = conversationRepo.findConversationsByParticipantId(user.getId());

        List<ChatHeadDto> dtos = new ArrayList<>();

        for (ConversationEntity convo : conversations) {
            ChatHeadDto dto = new ChatHeadDto();

            List<MessageEntity> messages = messageRepository.findAllMessagesByConversationId(convo.getId());

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

    public List<MessageResDto> getMessagesFromConversationId(UUID conversationId) {
        return messageRepository.findAllMessagesByConversationId(conversationId)
                .stream()
                .map(messageEntity -> new MessageResDto(
                        messageEntity.getFrom().getFullName(),
                        messageEntity.getMesssages(),
                        messageEntity.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void sendMessageByConversationId(UUID conversationId, SendMessageDto messageInfo) {

        ConversationEntity conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conversation not found"));

        MessageEntity message = new MessageEntity();
        message.setMesssages(messageInfo.getMessage());
        message.setConversation(conversation);

        if (messageInfo.getFiles() != null && !messageInfo.getFiles().isEmpty()) {
            List<FileEntity> files = messageInfo.getFiles().stream()
                    .map(file -> fileService.mapToFileEntity(file, message))
                    .collect(Collectors.toList());

            message.setFiles(files);
        }

        messageRepository.save(message);
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
