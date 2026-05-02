package com.powerstock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powerstock.event.LowStockEvent;
import com.powerstock.model.entity.Item;
import com.powerstock.model.entity.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Slf4j
@Service
public class AlertService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public AlertService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void checkAndAlert(Item item, Location location, int currentQuantity) {
        if (item.getReorderLevel() == null || item.getReorderLevel() <= 0) {
            return;
        }
        if (currentQuantity <= item.getReorderLevel()) {
            LowStockEvent event = LowStockEvent.builder()
                    .itemId(item.getId()).itemSku(item.getSku()).itemName(item.getName())
                    .locationId(location.getId()).locationName(location.getName())
                    .currentQuantity(currentQuantity).reorderLevel(item.getReorderLevel())
                    .timestamp(Instant.now()).build();
            String key = "alert:low-stock:" + item.getId() + ":" + location.getId();
            try {
                String json = objectMapper.writeValueAsString(event);
                redisTemplate.opsForValue().set(key, json);
                log.warn("Low stock alert: {} at {} - current: {}, reorder level: {}",
                        item.getSku(), location.getName(), currentQuantity, item.getReorderLevel());
            } catch (Exception e) {
                log.error("Failed to publish low stock alert for item {} at location {}",
                        item.getId(), location.getId(), e);
            }
        }
    }
}
