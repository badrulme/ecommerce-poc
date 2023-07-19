package com.badrul.ecommercepoc.repository;

import com.badrul.ecommercepoc.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    CustomerEntity findByLineUserId(String lineUserId);
}
