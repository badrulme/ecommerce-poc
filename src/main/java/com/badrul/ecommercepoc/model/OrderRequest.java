package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.enums.OrderFrom;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class OrderRequest {

    private OrderFrom orderFrom;

    private Long lineReservationId;

    private Long customerId;

    private String contactNo;

    private String shippingAddress;

    private List<OrderItemRequest> itemRequests;

    private Integer orderQuantity;

    private BigDecimal orderAmount;

}
