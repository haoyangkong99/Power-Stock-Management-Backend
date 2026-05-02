package com.powerstock.controller;

import com.powerstock.common.ApiResponse;
import com.powerstock.dto.response.InventoryValueResponse;
import com.powerstock.dto.response.LowStockItemResponse;
import com.powerstock.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('PERMISSION_REPORT_READ')")
    public ApiResponse<List<LowStockItemResponse>> getLowStockItems() {
        return ApiResponse.success(reportService.getLowStockItems());
    }

    @GetMapping("/inventory-value")
    @PreAuthorize("hasAuthority('PERMISSION_REPORT_READ')")
    public ApiResponse<InventoryValueResponse> getInventoryValue() {
        return ApiResponse.success(reportService.getInventoryValue());
    }
}
