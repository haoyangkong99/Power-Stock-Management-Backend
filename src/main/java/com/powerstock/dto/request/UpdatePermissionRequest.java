package com.powerstock.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotNull(message = "Permission mask is required") private Long permissionMask;
}
