package com.aaronjosh.real_estate_app.dto.payment;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentReqDto {
    private String coupon;

    @NotNull
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal amount;
}
