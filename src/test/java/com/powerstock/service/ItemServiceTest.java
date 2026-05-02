package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.ItemRequest;
import com.powerstock.dto.response.ItemResponse;
import com.powerstock.model.entity.Item;
import com.powerstock.repository.ItemRepository;
import com.powerstock.repository.PriceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PriceHistoryRepository priceHistoryRepository;
    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldCreateItem() {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-001");
        request.setName("Test Item");
        request.setBasePrice(BigDecimal.valueOf(10.00));
        request.setReorderLevel(5);
        request.setReorderQuantity(10);

        when(itemRepository.existsBySku("SKU-001")).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        ItemResponse response = itemService.createItem(request);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("SKU-001");
    }

    @Test
    void shouldThrowWhenCreatingDuplicateSku() {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-001");
        request.setName("Test Item");
        when(itemRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> itemService.createItem(request))
                .isInstanceOf(BusinessException.class).hasMessageContaining("SKU already exists");
    }

    @Test
    void shouldGetItemById() {
        Item item = Item.builder().sku("SKU-001").name("Test Item").basePrice(BigDecimal.TEN).build();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.getItemById(1L);
        assertThat(response.getSku()).isEqualTo("SKU-001");
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(999L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldListItemsPaginated() {
        Item list_item = Item.builder().sku("SKU-001").name("Item 1").build();
        list_item.setId(1L);
        Page<Item> page = new PageImpl<>(List.of(list_item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ItemResponse> result = itemService.getAllItems(Pageable.unpaged());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldUpdateItem() {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-001");
        request.setName("Updated Item");
        request.setBasePrice(BigDecimal.valueOf(15.00));

        Item existing = Item.builder().sku("SKU-001").name("Test Item").basePrice(BigDecimal.TEN).build();
        existing.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemResponse response = itemService.updateItem(1L, request);
        assertThat(response.getName()).isEqualTo("Updated Item");
    }

    @Test
    void shouldDeleteItem() {
        Item item = Item.builder().sku("SKU-001").name("Test Item").build();
        item.setId(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L);
    }
}
