package com.powerstock.service;

import com.powerstock.model.entity.InventoryStock;
import com.powerstock.model.entity.Item;
import com.powerstock.model.entity.Location;
import com.powerstock.repository.InventoryStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private InventoryStockRepository stockRepository;
    @InjectMocks private ReportService reportService;

    private Item testItem;
    private Location testLocation;
    private InventoryStock testStock;

    @BeforeEach
    void setUp() {
        testItem = Item.builder().sku("SKU-001").name("Widget").basePrice(BigDecimal.TEN).reorderLevel(5).build();
        testItem.setId(1L);
        testLocation = Location.builder().name("WH").build();
        testLocation.setId(1L);
        testStock = InventoryStock.builder().item(testItem).location(testLocation).currentQuantity(3).build();
        testStock.setId(1L);
    }

    @Test
    void shouldReturnLowStockItems() {
        when(stockRepository.findByCurrentQuantityLessThanEqual(Integer.MAX_VALUE)).thenReturn(List.of(testStock));
        var result = reportService.getLowStockItems();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemSku()).isEqualTo("SKU-001");
        assertThat(result.get(0).getCurrentQuantity()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyLowStockWhenAllAboveThreshold() {
        when(stockRepository.findByCurrentQuantityLessThanEqual(Integer.MAX_VALUE)).thenReturn(List.of());
        var result = reportService.getLowStockItems();
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateInventoryValue() {
        InventoryStock stock2 = InventoryStock.builder().item(testItem).location(testLocation).currentQuantity(7).build();
        when(stockRepository.findAll()).thenReturn(List.of(testStock, stock2));
        var result = reportService.getInventoryValue();
        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getTotalQuantity()).isEqualTo(10);
        assertThat(result.getTotalValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }
}
