package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.request.TransactionRequest;
import com.powerstock.dto.response.StockResponse;
import com.powerstock.dto.response.TransactionResponse;
import com.powerstock.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_TRANSACTION_CREATE')")
    public ApiResponse<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.createTransaction(request), "Transaction created");
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAuthority('PERMISSION_TRANSACTION_READ')")
    public ApiResponse<Page<TransactionResponse>> getTransactionsByItem(@PathVariable Long itemId, Pageable pageable) {
        return ApiResponse.success(transactionService.getTransactionsByItem(itemId, pageable));
    }

    @GetMapping("/stock/item/{itemId}")
    @PreAuthorize("hasAuthority('PERMISSION_TRANSACTION_READ')")
    public ApiResponse<List<StockResponse>> getStockByItem(@PathVariable Long itemId) {
        return ApiResponse.success(transactionService.getStockByItem(itemId));
    }

    @GetMapping("/stock/location/{locationId}")
    @PreAuthorize("hasAuthority('PERMISSION_TRANSACTION_READ')")
    public ApiResponse<List<StockResponse>> getStockByLocation(@PathVariable Long locationId) {
        return ApiResponse.success(transactionService.getStockByLocation(locationId));
    }
}
