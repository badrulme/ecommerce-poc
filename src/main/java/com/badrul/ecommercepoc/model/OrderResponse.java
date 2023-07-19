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

    private OrderFrom orderFrom;

    private CustomerResponse customer;

    private String mobileNo;

}
