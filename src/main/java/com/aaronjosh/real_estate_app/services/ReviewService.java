package com.aaronjosh.real_estate_app.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aaronjosh.real_estate_app.dto.booking.ReviewResDto;
import com.aaronjosh.real_estate_app.dto.booking.ReviewStatsResDto;
import com.aaronjosh.real_estate_app.models.ReviewEntity;
import com.aaronjosh.real_estate_app.models.UserEntity;
import com.aaronjosh.real_estate_app.repositories.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepo;

    public List<ReviewStatsResDto> getReviews(UUID propertyId) {
        List<ReviewEntity> reviews = reviewRepo.findByProperty_id(propertyId);
        List<ReviewStatsResDto> dtos = new ArrayList<>();

        for (ReviewEntity review : reviews) {
            ReviewStatsResDto dto = new ReviewStatsResDto();
            UserEntity user = review.getUser();
            dto.setTitle(user.getFullName());
            dto.setStars(review.getStars());
            dto.setMessage(review.getMessage());

            if (review.getConversation() != null && !review.getConversation().getMessages().isEmpty()) {
                List<ReviewResDto> reviewInfos = review.getConversation().getMessages()
                        .stream()
                        .map(messageInfo -> new ReviewResDto(
                                messageInfo.getFrom().getFirstname(),
                                messageInfo.getMesssages(),
                                messageInfo.getCreatedAt()))
                        .toList();
                dto.setReviews(reviewInfos);
            }

            dtos.add(dto);
        }

        return dtos;
    }
}
