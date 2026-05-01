package com.aaronjosh.real_estate_app.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.aaronjosh.real_estate_app.dto.property.PropertyCardDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyDto;
import com.aaronjosh.real_estate_app.dto.property.PropertyResDto;
import com.aaronjosh.real_estate_app.dto.property.UpdatePropertyDto;
import com.aaronjosh.real_estate_app.dto.user.UserDetails;
import com.aaronjosh.real_estate_app.models.FileEntity;
import com.aaronjosh.real_estate_app.models.PropertyStats;
import com.aaronjosh.real_estate_app.models.PropertyEntity;
import com.aaronjosh.real_estate_app.models.PropertyEntity.PropertyStatus;
import com.aaronjosh.real_estate_app.repositories.PropertyRepository;
import com.aaronjosh.real_estate_app.util.PropertyMapper;
import com.aaronjosh.real_estate_app.util.PropertyMapperWithSchedules;

@Service
@Transactional
public class PropertyService {

    @Autowired
    private UserService userService;

    @Autowired
    private PropertyRepository propertyRepo;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private PropertyMapperWithSchedules propertyMapperWithSchedules;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    // get all active properties
    @Transactional(readOnly = true)
    public Map<String, Object> getProperties(
            Integer page,
            String address,
            LocalDateTime start,
            LocalDateTime end,
            Integer minGuests,
            BigDecimal maxPrice,
            BigDecimal minPrice) {

        Pageable pageable = PageRequest.of(page - 1, 10);

        // Sanatize queries
        if (address != null) {
            address = "%" + address.toLowerCase() + "%";
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = BigDecimal.ZERO;
        }

        if (minGuests != null && minGuests < 1) {
            minGuests = 1;
        }

        Page<PropertyCardDto> properties = propertyRepo.fetchPropertyCards(pageable, address, start, end, minGuests,
                maxPrice, minPrice);

        return Map.of(
                "success", true,
                "properties", properties);
    }

    // gets the owner properties
    @Transactional(readOnly = true)
    public List<PropertyResDto> getMyPropeties() {
        UserDetails user = userService.getUserDetails();
        List<PropertyEntity> properties = propertyRepo.findByHostId(user.getId());

        return propertyMapper.toDto(properties);
    }

    // get property by id
    @Transactional(readOnly = true)
    public PropertyResDto getPropertyById(UUID propertyId) {
        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new RuntimeException("Property not found"));

        return propertyMapperWithSchedules.toDto(property);
    }

    public PropertyEntity addProperty(PropertyDto propertyDto) {
        // creating new property object
        PropertyEntity property = new PropertyEntity();

        // setting property info
        property.setTitle(propertyDto.getTitle());
        property.setDescription(propertyDto.getDescription());
        property.setPrice(propertyDto.getPrice());
        property.setPropertyType(propertyDto.getPropertyType());
        property.setMaxGuest(propertyDto.getMaxGuest());
        property.setTotalBedroom(propertyDto.getTotalBedroom());
        property.setTotalBed(propertyDto.getTotalBed());
        property.setTotalBath(propertyDto.getTotalBath());
        property.setAddress(propertyDto.getAddress());
        property.setCity(propertyDto.getCity());
        property.setStatus(PropertyStatus.ACTIVE);

        // adding the relation of images to property
        for (MultipartFile image : propertyDto.getImages()) {
            try {
                FileEntity file = new FileEntity();

                String key = cloudflareR2Service.uploadFile(image, true, "properties/" + property.getId() + "/images/");

                file.setFilename(image.getOriginalFilename());
                file.setSize(image.getSize());
                file.setContentType(image.getContentType());
                file.setKey(key);
                file.setPropertyEntity(property);
                file.setIsPublic(true);

                property.getImages().add(file);
            } catch (java.io.IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image file", e);
            }
        }

        property.setHost(userService.getUser());

        PropertyStats stats = new PropertyStats();
        stats.setProperty(property);
        property.setStats(stats);

        // saving the property
        return propertyRepo.save(property);
    }

    @Transactional
    public PropertyEntity editProperty(UpdatePropertyDto propertyDto, UUID propertyId) {
        UserDetails user = userService.getUserDetails();

        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Property not found"));

        UUID hostId = property.getHost().getId();
        UUID userId = user.getId();

        if (!userId.equals(hostId)) {
            throw new RuntimeException("You dont have access");
        }

        // Updates property details
        property.setTitle(propertyDto.getTitle());
        property.setDescription(propertyDto.getDescription());
        property.setPrice(propertyDto.getPrice());
        property.setPropertyType(propertyDto.getPropertyType());
        property.setMaxGuest(propertyDto.getMaxGuest());
        property.setTotalBedroom(propertyDto.getTotalBedroom());
        property.setTotalBed(propertyDto.getTotalBed());
        property.setTotalBath(propertyDto.getTotalBath());
        property.setAddress(propertyDto.getAddress());
        property.setCity(propertyDto.getCity());

        // Removes deleted images
        if (propertyDto.getRemovedImages() != null) {
            property.getImages().removeIf(image -> propertyDto.getRemovedImages().contains(image.getId()));
        }

        // Adds new images
        if (propertyDto.getNewImages() != null) {
            for (MultipartFile image : propertyDto.getNewImages()) {
                try {
                    FileEntity file = new FileEntity();

                    String key = cloudflareR2Service.uploadFile(image, true,
                            "properties/" + property.getId() + "/images/");

                    file.setFilename(image.getOriginalFilename());
                    file.setSize(image.getSize());
                    file.setContentType(image.getContentType());
                    file.setKey(key);
                    file.setPropertyEntity(property);

                    property.getImages().add(file);
                } catch (java.io.IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image file",
                            e);
                }
            }
        }

        return propertyRepo.save(property);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProperty(UUID propertyId) {
        PropertyEntity property = propertyRepo.findById(Objects.requireNonNull(propertyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Property not found"));

        UUID hostId = property.getHost().getId();
        UUID userId = userService.getUserDetails().getId();

        if (!hostId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to delete this property");
        }

        propertyRepo.delete(property);
    }

}
