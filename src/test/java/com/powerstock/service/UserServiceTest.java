package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.UpdateUserRequest;
import com.powerstock.dto.response.UserResponse;
import com.powerstock.model.entity.Location;
import com.powerstock.model.entity.User;
import com.powerstock.repository.LocationRepository;
import com.powerstock.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LocationRepository locationRepository;
    @InjectMocks private UserService userService;

    @Test
    void shouldGetUserById() {
        Location location = Location.builder().name("WH").build();
        location.setId(1L);
        User user = User.builder().username("user1").email("u@t.com").passwordHash("hash")
                .location(location).permissionMask(0L).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponse response = userService.getUserById(1L);
        assertThat(response.getUsername()).isEqualTo("user1");
        assertThat(response.getLocationName()).isEqualTo("WH");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(999L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldListAllUsers() {
        User user = User.builder().username("user1").email("u@t.com").build();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserResponse> users = userService.getAllUsers();
        assertThat(users).hasSize(1);
    }

    @Test
    void shouldUpdateUser() {
        Location newLoc = Location.builder().name("New WH").build();
        newLoc.setId(2L);
        User existing = User.builder().username("user1").email("old@t.com").build();
        existing.setId(1L);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@t.com");
        request.setLocationId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(newLoc));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserResponse response = userService.updateUser(1L, request);
        assertThat(response.getEmail()).isEqualTo("new@t.com");
    }

    @Test
    void shouldUpdatePermissions() {
        User user = User.builder().username("user1").permissionMask(0L).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        userService.updatePermissions(1L, 3L);
        assertThat(user.getPermissionMask()).isEqualTo(3L);
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder().username("user1").build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);
    }
}
