package com.aaronjosh.real_estate_app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.aaronjosh.real_estate_app.dto.auth.UserDetails;
import com.aaronjosh.real_estate_app.dto.property.ImageDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.repositories.FavoriteRepository;
import com.aaronjosh.real_estate_app.services.UserService;

@Component
public class PropertyMapper {
    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Value("${CLOUDFLARE_R2_PUBLIC_URL}")
    private String publicUrl;

    public PropertyResDto toDto(PropertyEntity property) {
        List<ImageDto> images = new ArrayList<>();
        property.getImages().forEach(image -> {
            String imageUrl = publicUrl + "/" + image.getKey();

            ImageDto imageDto = new ImageDto();
            imageDto.setId(image.getId());
            imageDto.setUrl(imageUrl);
            images.add(imageDto);
        });

        // Create a new dto instance
        PropertyResDto dto = new PropertyResDto();

        // Add property details to dto
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setAddress(property.getAddress());
        dto.setCity(property.getCity());
        dto.setDescription(property.getDescription());
        dto.setMaxGuest(property.getMaxGuest());
        dto.setPrice(property.getPrice());
        dto.setPropertyType(property.getPropertyType());
        dto.setTotalBath(property.getTotalBath());
        dto.setTotalBed(property.getTotalBed());
        dto.setTotalBedroom(property.getTotalBedroom());
        dto.setStatus(property.getStatus());
        dto.setImages(images);

        // Get the current user details
        UserDetails user = userService.getUserDetails();

        if (user != null) {
            // Checks if the property is on user's favorites
            boolean isFavorite = favoriteRepository.findByProperty_IdAndUser_Id(property.getId(), user.getId())
                    .isPresent();

            // Sets the favorite status to dto
            dto.setIsFavorite(isFavorite);
        }

        // returns the dto with the property details
        return dto;
    }

    public List<PropertyResDto> toDto(List<PropertyEntity> properties) {
        // Map each entity to a DTO using the single entity toDto method
        return properties.stream().map(this::toDto).collect(Collectors.toList());
    }
}
