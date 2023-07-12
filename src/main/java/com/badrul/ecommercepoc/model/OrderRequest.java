package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.entity.ProductEntity;
import com.badrul.ecommercepoc.enums.OrderFrom;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderRequest {

    private String code;

    private Long productId;

    private BigDecimal amount;

    private OrderFrom orderFrom;

    private String lineUserId;

    private String customerName;

    private String mobileNo;

}
