package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.enums.UserFrom;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {
    private Long id;
    private String name;
    private String lineUserId;
    private UserFrom userFrom;
    private String address;
    private String contactNo;

}
