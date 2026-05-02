package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.ItemRequest;
import com.powerstock.dto.response.ItemResponse;
import com.powerstock.model.entity.Item;
import com.powerstock.model.entity.PriceHistory;
import com.powerstock.repository.ItemRepository;
import com.powerstock.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        if (itemRepository.existsBySku(request.getSku()))
            throw new BusinessException("DUPLICATE_SKU", "SKU already exists: " + request.getSku());

        Item item = Item.builder()
                .sku(request.getSku()).name(request.getName())
                .description(request.getDescription()).category(request.getCategory())
                .basePrice(request.getBasePrice())
                .reorderLevel(request.getReorderLevel()).reorderQuantity(request.getReorderQuantity())
                .build();

        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ITEM_NOT_FOUND", "Item not found: " + id));

        if (!item.getSku().equals(request.getSku()) && itemRepository.existsBySku(request.getSku()))
            throw new BusinessException("DUPLICATE_SKU", "SKU already exists: " + request.getSku());

        item.setSku(request.getSku());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setReorderLevel(request.getReorderLevel());
        item.setReorderQuantity(request.getReorderQuantity());

        if (item.getBasePrice().compareTo(request.getBasePrice()) != 0) {
            recordPriceChange(item, item.getBasePrice(), request.getBasePrice());
            item.setBasePrice(request.getBasePrice());
        }

        return toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ITEM_NOT_FOUND", "Item not found: " + id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItems(String query, Pageable pageable) {
        return itemRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ITEM_NOT_FOUND", "Item not found: " + id));
        itemRepository.delete(item);
    }

    private void recordPriceChange(Item item, BigDecimal oldPrice, BigDecimal newPrice) {
        PriceHistory history = PriceHistory.builder()
                .item(item).oldPrice(oldPrice).newPrice(newPrice).build();
        priceHistoryRepository.save(history);
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId()).sku(item.getSku()).name(item.getName())
                .description(item.getDescription()).category(item.getCategory())
                .basePrice(item.getBasePrice()).reorderLevel(item.getReorderLevel())
                .reorderQuantity(item.getReorderQuantity())
                .createdAt(item.getCreatedAt()).updatedAt(item.getUpdatedAt())
                .build();
    }
}
