package com.powerstock.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email private String email;
    private Long locationId;
}
