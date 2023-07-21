package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.OrderEntity;
import com.badrul.ecommercepoc.entity.OrderItemEntity;
import com.badrul.ecommercepoc.entity.ProductEntity;
import com.badrul.ecommercepoc.model.OrderItemRequest;
import com.badrul.ecommercepoc.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {

    private final ProductService productService;

    private final OrderItemRepository repository;


    List<OrderItemEntity> create(List<OrderItemRequest> itemRequest, OrderEntity orderEntity) {

        List<OrderItemEntity> orderItemEntities = itemRequest.stream()
                .map(request -> getOrderItemEntity(request, orderEntity)).toList();

        return repository.saveAll(orderItemEntities);
    }

    private OrderItemEntity getOrderItemEntity(OrderItemRequest request, OrderEntity orderEntity) {
        ProductEntity product = productService.getProductEntity(request.getProductId());

        OrderItemEntity orderItem = new OrderItemEntity();

        orderItem.setOrderQuantity(request.getOrderQuantity());
        orderItem.setProduct(product);
        orderItem.setProductPrice(product.getPrice());
        orderItem.setOrder(orderEntity);

        return orderItem;

    }
}
