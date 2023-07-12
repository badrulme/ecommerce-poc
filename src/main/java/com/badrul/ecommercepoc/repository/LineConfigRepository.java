package com.badrul.ecommercepoc.repository;

import com.badrul.ecommercepoc.entity.LineConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LineConfigRepository extends JpaRepository<LineConfigEntity, Long> {

    LineConfigEntity findByBotUserIdAndIsDeleted(String botUserId, boolean b);

    Optional<LineConfigEntity> findByIdAndIsDeleted(Long id, boolean b);

}
