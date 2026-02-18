package com.aaronjosh.real_estate_app.dto.payment;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentReqDto {
    private String coupon;
    private BigDecimal amount;
}
