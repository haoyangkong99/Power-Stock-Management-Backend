package com.powerstock.service;

import com.powerstock.dto.response.InventoryValueResponse;
import com.powerstock.dto.response.LowStockItemResponse;
import com.powerstock.model.entity.InventoryStock;
import com.powerstock.model.entity.Item;
import com.powerstock.repository.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InventoryStockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<LowStockItemResponse> getLowStockItems() {
        return stockRepository.findByCurrentQuantityLessThanEqual(Integer.MAX_VALUE).stream()
                .filter(stock -> stock.getItem().getReorderLevel() != null
                        && stock.getItem().getReorderLevel() > 0
                        && stock.getCurrentQuantity() <= stock.getItem().getReorderLevel())
                .map(this::toLowStockResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryValueResponse getInventoryValue() {
        List<InventoryStock> allStocks = stockRepository.findAll();
        Map<Long, List<InventoryStock>> groupedByItem = allStocks.stream()
                .collect(Collectors.groupingBy(stock -> stock.getItem().getId()));

        List<InventoryValueResponse.ItemValueBreakdown> breakdowns = groupedByItem.values().stream()
                .map(stocks -> {
                    InventoryStock first = stocks.get(0);
                    Item item = first.getItem();
                    int totalQty = stocks.stream().mapToInt(InventoryStock::getCurrentQuantity).sum();
                    BigDecimal totalItemValue = item.getBasePrice().multiply(BigDecimal.valueOf(totalQty));
                    return InventoryValueResponse.ItemValueBreakdown.builder()
                            .itemId(item.getId()).itemSku(item.getSku()).itemName(item.getName())
                            .totalQuantity(totalQty).unitPrice(item.getBasePrice())
                            .totalItemValue(totalItemValue).build();
                }).toList();

        int totalQuantity = allStocks.stream().mapToInt(InventoryStock::getCurrentQuantity).sum();
        BigDecimal totalValue = breakdowns.stream()
                .map(InventoryValueResponse.ItemValueBreakdown::getTotalItemValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InventoryValueResponse.builder()
                .totalItems(breakdowns.size()).totalQuantity(totalQuantity)
                .totalValue(totalValue).breakdowns(breakdowns).build();
    }

    private LowStockItemResponse toLowStockResponse(InventoryStock stock) {
        return LowStockItemResponse.builder()
                .itemId(stock.getItem().getId()).itemSku(stock.getItem().getSku())
                .itemName(stock.getItem().getName()).locationId(stock.getLocation().getId())
                .locationName(stock.getLocation().getName()).currentQuantity(stock.getCurrentQuantity())
                .reorderLevel(stock.getItem().getReorderLevel())
                .reorderQuantity(BigDecimal.valueOf(stock.getItem().getReorderQuantity()))
                .build();
    }
}
