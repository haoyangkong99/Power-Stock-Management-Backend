package com.powerstock.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LocationRequest {
    @NotBlank(message = "Location name is required") private String name;
    private String address;
}
