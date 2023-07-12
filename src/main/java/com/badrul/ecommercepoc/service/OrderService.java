package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.OrderEntity;
import com.badrul.ecommercepoc.enums.OrderFrom;
import com.badrul.ecommercepoc.model.OrderRequest;
import com.badrul.ecommercepoc.model.OrderResponse;
import com.badrul.ecommercepoc.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository repository;
    private final ProductService productService;

    public OrderResponse create(OrderRequest request) {

        return getOrderResponse(repository.save(getOrderEntity(request, new OrderEntity())));

    }

    public OrderResponse change(OrderRequest request, Long id) {

        OrderEntity entity = getOrderEntity(id);

        return getOrderResponse(repository.save(getOrderEntity(request, entity)));

    }

    public List<OrderResponse> getAllOrders() {
        return repository.findAll().stream().map(this::getOrderResponse).toList();
    }

    public OrderResponse getById(Long id) {
        return getOrderResponse(getOrderEntity(id));
    }


    public void remove(Long id) {
        repository.delete(getOrderEntity(id));
    }

    private OrderEntity getOrderEntity(OrderRequest request, OrderEntity entity) {

        entity.setCode(request.getCode());
        entity.setOrderFrom(request.getOrderFrom());
        entity.setAmount(request.getAmount());
        entity.setCustomerName(request.getCustomerName());
        entity.setMobileNo(request.getMobileNo());

        entity.setProduct(productService.getProductEntity(request.getProductId()));

        if (OrderFrom.LINE.equals(request.getOrderFrom())) {
            entity.setLineUserId(request.getLineUserId());
        }
        return entity;
    }

    private OrderEntity getOrderEntity(Long id) {
        return repository.getReferenceById(id);
    }

    private OrderResponse getOrderResponse(OrderEntity entity) {
        OrderResponse response = new OrderResponse();

        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setOrderFrom(entity.getOrderFrom());
        response.setAmount(entity.getAmount());
        response.setCustomerName(entity.getCustomerName());
        response.setMobileNo(entity.getMobileNo());
        response.setLineUserId(entity.getLineUserId());

        return response;
    }


}
