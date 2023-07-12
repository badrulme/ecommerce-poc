package com.badrul.ecommercepoc.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductResponse {
    private Long id;

    private String code;

    private String title;

    private String description;

    private BigDecimal price;

    private String productImageUrl;

    private int rating;

}
