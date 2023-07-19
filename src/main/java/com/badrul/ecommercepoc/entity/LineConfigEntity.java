package com.badrul.ecommercepoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Line Config class
 *
 * @author Badrul
 */
@Entity
@Setter
@Getter
@Table(name = "line_config")
public class LineConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "channel_secret", nullable = false, length = 50)
    private String channelSecret;

    @Column(name = "channel_access_token", nullable = false)
    private String channelAccessToken;

    @Column(name = "bot_basic_id", nullable = false, length = 50)
    private String botBasicId;

    @Column(name = "channel_name", nullable = false, length = 50)
    private String channelName;

    @Column(name = "bot_user_id", nullable = false, length = 50, unique = true)
    private String botUserId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;


}
