package com.powerstock.repository;

import com.powerstock.model.entity.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldSaveAndFindItem() {
        Item item = new Item();
        item.setSku("TEST-001");
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setCategory("Test Category");
        item.setBasePrice(java.math.BigDecimal.valueOf(10.00));
        item.setReorderLevel(5);
        item.setReorderQuantity(10);

        Item saved = itemRepository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSku()).isEqualTo("TEST-001");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}