package com.badrul.ecommercepoc.entity;

import com.badrul.ecommercepoc.enums.OrderFrom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @OneToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderFrom orderFrom;

    private String lineUserId;

    private String customerName;

    @Column(length = 30)
    private String mobileNo;


}
