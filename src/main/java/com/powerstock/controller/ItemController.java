package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.request.ItemRequest;
import com.powerstock.dto.response.ItemResponse;
import com.powerstock.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_ITEM_READ')")
    public ApiResponse<Page<ItemResponse>> getItems(@RequestParam(required = false) String search, Pageable pageable) {
        if (search != null && !search.isBlank())
            return ApiResponse.success(itemService.searchItems(search, pageable));
        return ApiResponse.success(itemService.getAllItems(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ITEM_READ')")
    public ApiResponse<ItemResponse> getItem(@PathVariable Long id) {
        return ApiResponse.success(itemService.getItemById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_ITEM_CREATE')")
    public ApiResponse<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        return ApiResponse.success(itemService.createItem(request), "Item created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ITEM_UPDATE')")
    public ApiResponse<ItemResponse> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ApiResponse.success(itemService.updateItem(id, request), "Item updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_ITEM_DELETE')")
    public ApiResponse<Map<String, Object>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ApiResponse.success(Map.of("deleted", true), "Item deleted successfully");
    }
}
