package com.badrul.ecommercepoc.repository;

import com.badrul.ecommercepoc.entity.LineReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineReservationRepository extends JpaRepository<LineReservationEntity, Long> {
    LineReservationEntity findFirstByBotUserIdAndUserIdOrderByIdDesc(String botUserId, String userId);

    LineReservationEntity findFirstByBotUserIdAndUserIdAndUserIdAndReservationCompleted_FalseOrderByIdDesc(String botUserId, String userId);

}
