package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.UpdateUserRequest;
import com.powerstock.dto.response.UserResponse;
import com.powerstock.model.entity.Location;
import com.powerstock.model.entity.User;
import com.powerstock.repository.LocationRepository;
import com.powerstock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found: " + id));
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Location not found"));
            user.setLocation(location);
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void updatePermissions(Long id, Long permissionMask) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found: " + id));
        user.setPermissionMask(permissionMask);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found: " + id));
        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .locationId(user.getLocation() != null ? user.getLocation().getId() : null)
                .locationName(user.getLocation() != null ? user.getLocation().getName() : null)
                .permissionMask(user.getPermissionMask()).createdAt(user.getCreatedAt())
                .build();
    }
}
