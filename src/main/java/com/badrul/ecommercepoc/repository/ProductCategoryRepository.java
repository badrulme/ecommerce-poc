package com.badrul.ecommercepoc.repository;

import com.badrul.ecommercepoc.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductEntity, Long> {
}
