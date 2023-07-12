package com.badrul.ecommercepoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Line Reservation entity
 *
 * @author Badrul
 */
@Entity
@Table(name = "line_reservation")
@Setter
@Getter
public class LineReservationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "bot_user_id", nullable = false, length = 250)
    private String botUserId;

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;

    @Column(name = "reservation_completed", nullable = false)
    private boolean reservationCompleted = false;
}
