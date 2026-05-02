package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.request.UpdatePermissionRequest;
import com.powerstock.dto.request.UpdateUserRequest;
import com.powerstock.dto.response.UserResponse;
import com.powerstock.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_USER_READ')")
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_USER_READ')")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_USER_UPDATE')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.updateUser(id, request), "User updated");
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_PERMISSION_MANAGE')")
    public ApiResponse<UserResponse> updatePermissions(@PathVariable Long id,
                                                       @Valid @RequestBody UpdatePermissionRequest request) {
        userService.updatePermissions(id, request.getPermissionMask());
        return ApiResponse.success(userService.getUserById(id), "Permissions updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_USER_DELETE')")
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(Map.of("deleted", true), "User deleted");
    }
}
