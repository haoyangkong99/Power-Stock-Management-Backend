package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.request.UnitRequest;
import com.powerstock.dto.response.UnitResponse;
import com.powerstock.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_UNIT_READ')")
    public ApiResponse<List<UnitResponse>> getUnits() {
        return ApiResponse.success(unitService.getAllUnits());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_UNIT_CREATE')")
    public ApiResponse<UnitResponse> createUnit(@Valid @RequestBody UnitRequest request) {
        return ApiResponse.success(unitService.createUnit(request), "Unit created");
    }
}
