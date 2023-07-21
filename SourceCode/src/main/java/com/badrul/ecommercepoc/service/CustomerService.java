package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.CustomerEntity;
import com.badrul.ecommercepoc.model.CustomerRequest;
import com.badrul.ecommercepoc.model.CustomerResponse;
import com.badrul.ecommercepoc.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {
    private final CustomerRepository repository;

    public Long create(CustomerRequest request) {

        CustomerEntity entity = new CustomerEntity();

        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setContactNo(request.getContactNo());
        entity.setUserFrom(request.getUserFrom());
        entity.setLineUserId(request.getLineUserId());

        return repository.save(entity).getId();
    }

    public CustomerEntity getCustomerEntity(Long id) {
        return repository.getReferenceById(id);
    }

    public CustomerEntity getCustomerEntity(String lineUserId) {
        return repository.findByLineUserId(lineUserId);
    }

    public CustomerResponse getCustomer(CustomerEntity customer) {

        CustomerResponse response = new CustomerResponse();

        response.setAddress(customer.getAddress());
        response.setName(customer.getName());
        response.setId(customer.getId());
        response.setUserFrom(customer.getUserFrom());
        response.setLineUserId(customer.getLineUserId());

        return response;
    }

    public List<CustomerResponse> getAllCustomers() {
        return repository.findAll().stream().map(this::getCustomer).toList();
    }
}
