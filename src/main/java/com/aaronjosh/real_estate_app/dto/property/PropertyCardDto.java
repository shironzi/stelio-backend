package com.aaronjosh.real_estate_app.dto.property;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class PropertyCardDto {

    private UUID id;
    private String title;
    private String address;
    private String imageUrl;
    private BigDecimal price;

    public PropertyCardDto(UUID id, String title, String address, BigDecimal price, String imageKey) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.imageUrl = buildUrl(imageKey);
    }

    private static String buildUrl(String imageKey) {
        String publicUrl = System.getenv("CLOUDFLARE_R2_PUBLIC_URL");
        return imageKey != null ? publicUrl + "/" + imageKey : null;
    }
}
