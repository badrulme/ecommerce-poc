package com.badrul.ecommercepoc.entity;

import com.badrul.ecommercepoc.enums.OrderFrom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    private Long productId;

    private BigDecimal amount;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private OrderFrom orderFrom;

    private String lineUserId;

    @ManyToOne
    private LineReservationEntity lineReservation;

    private String customerName;

    @Column(length = 30)
    private String contactNo;


}
