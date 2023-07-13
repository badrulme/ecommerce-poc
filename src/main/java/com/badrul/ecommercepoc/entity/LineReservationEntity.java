package com.badrul.ecommercepoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "reservation")
    private List<LineReservationItemEntity> lineReservationItems;

}
