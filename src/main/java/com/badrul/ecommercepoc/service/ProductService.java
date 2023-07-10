package com.badrul.ecommercepoc.service;

import com.badrul.ecommercepoc.entity.ProductEntity;
import com.badrul.ecommercepoc.model.ProductRequest;
import com.badrul.ecommercepoc.model.ProductResponse;
import com.badrul.ecommercepoc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;


    public ProductResponse create(ProductRequest request) {

        return getProductResponse(repository.save(getProductEntity(request, new ProductEntity())));

    }

    public ProductResponse change(ProductRequest request, Long id) {

        ProductEntity entity = getProductEntity(id);

        return getProductResponse(repository.save(getProductEntity(request, entity)));

    }

    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream().map(this::getProductResponse).toList();
    }

    public ProductResponse getById(Long id) {
        return getProductResponse(getProductEntity(id));
    }


    public void remove(Long id) {
        repository.delete(getProductEntity(id));
    }

    private ProductEntity getProductEntity(ProductRequest request, ProductEntity entity) {

        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());

        return entity;
    }

    private ProductEntity getProductEntity(Long id) {
        return repository.getReferenceById(id);
    }

    private ProductResponse getProductResponse(ProductEntity entity) {
        ProductResponse response = new ProductResponse();

        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getPrice());

        return response;
    }
}
