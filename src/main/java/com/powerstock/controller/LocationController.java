package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.request.LocationRequest;
import com.powerstock.dto.response.LocationResponse;
import com.powerstock.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_LOCATION_READ')")
    public ApiResponse<List<LocationResponse>> getLocations() {
        return ApiResponse.success(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_LOCATION_READ')")
    public ApiResponse<LocationResponse> getLocation(@PathVariable Long id) {
        return ApiResponse.success(locationService.getLocationById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_LOCATION_CREATE')")
    public ApiResponse<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.success(locationService.createLocation(request), "Location created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_LOCATION_UPDATE')")
    public ApiResponse<LocationResponse> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return ApiResponse.success(locationService.updateLocation(id, request), "Location updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_LOCATION_DELETE')")
    public ApiResponse<Map<String, Object>> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ApiResponse.success(Map.of("deleted", true), "Location deleted");
    }
}
