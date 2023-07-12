package com.badrul.ecommercepoc.repository;

import com.badrul.ecommercepoc.entity.LineReservationEntity;
import com.badrul.ecommercepoc.entity.LineReservationItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineReservationItemRepository extends JpaRepository<LineReservationItemEntity, Long> {

    LineReservationItemEntity findFirstByReservationIdOrderByIdDesc(Long reservationId);

    List<LineReservationItemEntity> findAllByReservationIdOrderById(Long reservationId);


}
