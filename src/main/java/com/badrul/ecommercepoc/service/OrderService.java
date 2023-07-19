package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.OrderEntity;
import com.badrul.ecommercepoc.model.OrderRequest;
import com.badrul.ecommercepoc.model.OrderResponse;
import com.badrul.ecommercepoc.repository.LineReservationRepository;
import com.badrul.ecommercepoc.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository repository;
    private final LineReservationRepository lineReservationRepository;
    private final OrderItemService orderItemService;
    private final CustomerService customerService;

    public Long create(OrderRequest request) {
        OrderEntity order = repository.save(getOrderEntity(request, new OrderEntity()));

        orderItemService.create(request.getItemRequests(), order);
        return order.getId();
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

        entity.setCode(UUID.randomUUID().toString());
        entity.setOrderFrom(request.getOrderFrom());
        entity.setContactNo(request.getContactNo());
        entity.setShippingAddress(request.getShippingAddress());
        entity.setLineReservation(lineReservationRepository.getReferenceById(request.getLineReservationId()));
        entity.setCustomer(customerService.getCustomerEntity(request.getCustomerId()));

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
        response.setCustomer(customerService.getCustomer(entity.getCustomer()));
        response.setMobileNo(entity.getContactNo());

        return response;
    }


}
