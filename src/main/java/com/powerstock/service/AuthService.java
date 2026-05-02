package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.LoginRequest;
import com.powerstock.dto.request.RegisterRequest;
import com.powerstock.dto.response.AuthResponse;
import com.powerstock.model.entity.Location;
import com.powerstock.model.entity.User;
import com.powerstock.model.enums.Permission;
import com.powerstock.repository.LocationRepository;
import com.powerstock.repository.UserRepository;
import com.powerstock.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Location not found"));
        if (userRepository.existsByUsername(request.getUsername()))
            throw new BusinessException("DUPLICATE_USERNAME", "Username already exists");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BusinessException("DUPLICATE_EMAIL", "Email already exists");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .location(createLocationReference(request.getLocationId()))
                .permissionMask(0L)
                .build();
        user = userRepository.save(user);

        long defaultPermissions = Permission.ITEM_READ.getMask()
                | Permission.TRANSACTION_READ.getMask()
                | Permission.LOCATION_READ.getMask()
                | Permission.UNIT_READ.getMask();
        user.setPermissionMask(defaultPermissions);
        user = userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .username(user.getUsername())
                .email(user.getEmail())
                .permissionMask(user.getPermissionMask())
                .build();
    }

    private Location createLocationReference(Long locationId) {
        Location location = new Location();
        location.setId(locationId);
        return location;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username or password");

        return AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .username(user.getUsername())
                .email(user.getEmail())
                .permissionMask(user.getPermissionMask())
                .build();
    }
}
