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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private OrderFrom orderFrom;

    @ManyToOne
    private LineReservationEntity lineReservation;

    @ManyToOne(optional = false)
    private CustomerEntity customer;

    @Column(length = 30)
    private String contactNo;

    private String shippingAddress;

    private Integer orderQuantity;

    private BigDecimal orderAmount;

    @OneToMany(mappedBy = "order")
    private List<OrderItemEntity> orderItems;


}
