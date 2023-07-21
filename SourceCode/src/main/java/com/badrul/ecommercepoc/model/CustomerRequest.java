package com.badrul.ecommercepoc.model;

import com.badrul.ecommercepoc.enums.UserFrom;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotEmpty
    private String name;
    private String lineUserId;
    @NotNull
    private UserFrom userFrom;
    private String address;
    private String contactNo;

}
