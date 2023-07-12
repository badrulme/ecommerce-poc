package com.badrul.ecommercepoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Line Reservation Item entity
 *
 * @author Badrul
 */
@Entity
@Table(name = "line_reservation_item")
@Setter
@Getter
public class LineReservationItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne
    private LineReservationEntity reservation;

    @Column(name = "line_message_id", length = 100)
    private String lineMessageId;

    @Column(name = "line_text_message", nullable = false, length = 250)
    private String lineTextMessage;

    @Column(name = "message_step_no")
    private Integer messageStepNo;

    @Column(name = "user_id", nullable = false, length = 150)
    private String userId;
}
