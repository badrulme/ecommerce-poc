package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.OrderEntity;
import com.badrul.ecommercepoc.enums.OrderFrom;
import com.badrul.ecommercepoc.model.OrderRequest;
import com.badrul.ecommercepoc.model.OrderResponse;
import com.badrul.ecommercepoc.repository.LineReservationRepository;
import com.badrul.ecommercepoc.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.random.RandomGenerator;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository repository;
    private final ProductService productService;
    private final LineReservationRepository lineReservationRepository;

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

        entity.setCode(RandomGenerator.getDefault().toString());
        entity.setOrderFrom(request.getOrderFrom());
        entity.setAmount(request.getAmount());
        entity.setCustomerName(request.getCustomerName());
        entity.setContactNo(request.getContactNo());
        entity.setQuantity(request.getQuantity());

        entity.setProductId(request.getProductId());

        if (OrderFrom.LINE.equals(request.getOrderFrom())) {
            entity.setLineUserId(request.getLineUserId());
            entity.setLineReservation(lineReservationRepository.getReferenceById(request.getLineReservationId()));
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
        response.setMobileNo(entity.getContactNo());
        response.setLineUserId(entity.getLineUserId());

        return response;
    }


}
