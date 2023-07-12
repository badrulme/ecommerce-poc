package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.enums.OrderFrom;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderResponse {

    private Long id;

    private String code;

    private Long productId;

    private BigDecimal amount;

    private OrderFrom orderFrom;

    private String lineUserId;

    private String customerName;

    private String mobileNo;

}
