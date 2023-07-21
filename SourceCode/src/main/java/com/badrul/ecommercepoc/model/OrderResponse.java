package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.enums.OrderFrom;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class OrderResponse {

    private Long id;

    private String code;

    private LocalDateTime date;

    private OrderFrom orderFrom;

    private CustomerResponse customer;

    private Integer orderQuantity;

    private BigDecimal orderAmount;

    private String mobileNo;

    private String shippingAddress;

}
