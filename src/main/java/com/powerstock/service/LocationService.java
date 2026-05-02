package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.LocationRequest;
import com.powerstock.dto.response.LocationResponse;
import com.powerstock.model.entity.Location;
import com.powerstock.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        Location location = Location.builder().name(request.getName()).address(request.getAddress()).active(true).build();
        return toResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Location not found: " + id));
        return toResponse(location);
    }

    @Transactional
    public LocationResponse updateLocation(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Location not found: " + id));
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Location not found: " + id));
        location.setActive(false);
        locationRepository.save(location);
    }

    private LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId()).name(location.getName()).address(location.getAddress())
                .active(location.getActive()).createdAt(location.getCreatedAt()).updatedAt(location.getUpdatedAt())
                .build();
    }
}
