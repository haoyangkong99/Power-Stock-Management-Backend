package com.powerstock.service;

import com.powerstock.model.entity.Item;
import com.powerstock.model.entity.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @InjectMocks private AlertService alertService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldPublishLowStockAlert() {
        Item item = Item.builder().name("Widget").sku("WDG-001").reorderLevel(10).build();
        item.setId(1L);
        Location location = Location.builder().name("Main Warehouse").build();
        location.setId(1L);
        alertService.checkAndAlert(item, location, 5);
        verify(valueOperations).set(eq("alert:low-stock:1:1"), any(String.class));
    }

    @Test
    void shouldNotAlertWhenStockAboveReorderLevel() {
        Item item = Item.builder().name("Widget").sku("WDG-001").reorderLevel(5).build();
        item.setId(1L);
        Location location = Location.builder().name("Main Warehouse").build();
        location.setId(1L);
        alertService.checkAndAlert(item, location, 10);
        verify(valueOperations, never()).set(any(), any());
    }

    @Test
    void shouldNotAlertWhenReorderLevelIsZero() {
        Item item = Item.builder().name("Widget").sku("WDG-001").reorderLevel(0).build();
        item.setId(1L);
        Location location = Location.builder().name("Main Warehouse").build();
        location.setId(1L);
        alertService.checkAndAlert(item, location, 0);
        verify(valueOperations, never()).set(any(), any());
    }
}
