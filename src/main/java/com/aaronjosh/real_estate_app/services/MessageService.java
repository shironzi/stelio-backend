package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.aaronjosh.real_estate_app.dto.booking.BookingReqDto;
import com.aaronjosh.real_estate_app.dto.message.ChatHeadDto;
import com.aaronjosh.real_estate_app.dto.message.ConversationReqDto;
import com.aaronjosh.real_estate_app.dto.message.MessageDto;
import com.aaronjosh.real_estate_app.dto.message.MessageResDto;
import com.aaronjosh.real_estate_app.dto.message.SendMessageDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.models.ConversationEntity;
import com.aaronjosh.real_estate_app.models.MessageEntity;
import com.aaronjosh.real_estate_app.models.ParticipantEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.models.ParticipantEntity.MessageRole;
import com.aaronjosh.real_estate_app.repositories.ConversationRepository;
import com.aaronjosh.real_estate_app.repositories.MessageRepository;
import com.aaronjosh.real_estate_app.repositories.ParticipantRepo;
import com.aaronjosh.real_estate_app.repositories.UserRepository;
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
    private ParticipantRepo participantRepo;

    @Autowired
    private UserRepository userRepository;

    public List<ChatHeadDto> getChatHeads() {
        UserDetails user = userService.getUserDetails();

        List<ConversationEntity> conversations = conversationRepo.findByParticipantsWhoJoinedId(user.getId());

        List<ChatHeadDto> dtos = new ArrayList<>();

        for (ConversationEntity convo : conversations) {
            ChatHeadDto dto = new ChatHeadDto();

            List<MessageEntity> messages = messageRepository.findAllMessagesByConversationId(convo.getId());

            MessageEntity latestMessage = messages.stream().findFirst().orElse(null);

            if (latestMessage != null) {
                dto.setMessagePreview(latestMessage.getMesssages());
                dto.setDate(latestMessage.getCreatedAt());
            }

            String conversationName = convo.getParticipants().stream()
                    .filter(participant -> !participant.getWhoJoined().getId().equals(user.getId()))
                    .map(participant -> participant.getWhoJoined().getFullName())
                    .findFirst()
                    .orElse(null);

            dto.setChatName(conversationName != null ? conversationName : convo.getConversationName());
            dto.setProfileLink(null);
            dto.setConversationId(convo.getId());

            dtos.add(dto);
        }

        return dtos;
    }

    public List<MessageResDto> getMessagesFromConversationId(UUID conversationId) {

        UserDetails user = userService.getUserDetails();

        ParticipantEntity participant = participantRepo.findByConversationIdAndWhoJoinedId(conversationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation not found"));

        List<MessageEntity> messages = participant.getConversation().getMessages();

        return messages
                .stream()
                .sorted(Comparator.comparing(MessageEntity::getCreatedAt))
                .map(messageEntity -> new MessageResDto(
                        messageEntity.getId(),
                        messageEntity.getFrom().getId(),
                        messageEntity.getFrom().getFullName(),
                        messageEntity.getMesssages(),
                        messageEntity.getFiles().stream()
                                .map(file -> ServletUriComponentsBuilder.fromCurrentContextPath()
                                        .path("/api/image/")
                                        .path(file.getId().toString())
                                        .toUriString())
                                .toArray(String[]::new),
                        messageEntity.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void createNewConversation(ConversationReqDto participant) {
        UserEntity owner = userService.getUser();

        UserEntity receiptient = userRepository.findById(participant.getParticipantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (owner.getId().equals(receiptient)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot create a conversation with yourself");
        }

        ConversationEntity conversation = new ConversationEntity();

        // Conversation Owner
        ParticipantEntity conversationOwner = new ParticipantEntity();
        conversationOwner.setRole(MessageRole.ADMIN);
        conversationOwner.setWhoJoined(owner);
        conversationOwner.setConversation(conversation);

        // Conversation receipient
        ParticipantEntity conversationReceipient = new ParticipantEntity();
        conversationReceipient.setRole(MessageRole.MEMBER);
        conversationReceipient.setWhoJoined(receiptient);
        conversationReceipient.setConversation(conversation);

        List<ParticipantEntity> participants = new ArrayList<>();
        participants.add(conversationReceipient);
        participants.add(conversationOwner);

        conversation.setParticipants(participants);
        conversationRepo.save(conversation);
    }

    public MessageDto sendMessageByConversationId(UUID conversationId, SendMessageDto messageInfo) {
        if (messageInfo.getMessage() == null || messageInfo.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }

        UserEntity user = userService.getUser();

        ParticipantEntity participant = participantRepo.findByConversationIdAndWhoJoinedId(conversationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Conversation not found"));

        ConversationEntity conversation = participant.getConversation();

        MessageEntity message = new MessageEntity();
        message.setMesssages(messageInfo.getMessage());
        message.setConversation(conversation);
        message.setFrom(user);

        // if (messageInfo.getFiles() != null && !messageInfo.getFiles().isEmpty()) {
        // List<FileEntity> files = messageInfo.getFiles().stream()
        // .map(file -> fileService.mapToFileEntity(file, message))
        // .collect(Collectors.toList());

        // message.setFiles(files);
        // }

        messageRepository.save(message);

        MessageDto messageDto = new MessageDto();

        messageDto.setUserId(user.getId());
        messageDto.setMessage(message.getMesssages());
        // messageDto.setFilePaths(message.getFiles());
        messageDto.setName(user.getFullName());
        messageDto.setTimestamp(message.getCreatedAt());

        return messageDto;
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
