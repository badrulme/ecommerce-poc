package com.badrul.ecommercepoc.controller;

import com.badrul.ecommercepoc.model.ProductRequest;
import com.badrul.ecommercepoc.model.ProductResponse;
import com.badrul.ecommercepoc.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductService service;

    @PostMapping(consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.OK);
    }

    @PutMapping(value = "{id}", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<ProductResponse> change(@Valid @RequestBody ProductRequest request,
                                                  @PathVariable Long id) {
        return new ResponseEntity<>(service.change(request, id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return new ResponseEntity<>(service.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{id}")
    public void remove(@PathVariable Long id) {
        service.remove(id);
    }
}
