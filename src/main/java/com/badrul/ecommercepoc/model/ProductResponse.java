package com.badrul.ecommercepoc.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductResponse {
    private Long id;

    private String code;

    private String title;

    private String description;

    private Double price;


}
